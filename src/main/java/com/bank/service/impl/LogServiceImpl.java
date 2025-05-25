package com.bank.service.impl;

import com.bank.model.LogTask;
import com.bank.service.LogService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link LogService} for handling log processing tasks.
 */
@Service
public class LogServiceImpl implements LogService {

  private static final String LOGS_DIRECTORY = "./logs";
  private static final String MAIN_LOG_FILE = "./logs/application.log";
  private static final long INITIAL_DELAY_SECONDS = 1;
  private static final long PROCESSING_STEP_DELAY_SECONDS = 10;

  private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  private enum TaskStatus {
    CREATED("Задача создана"),
    QUEUED("В очереди на выполнение"),
    PREPARING("Подготовка к обработке"),
    READING("Чтение лог-файла"),
    FILTERING("Фильтрация записей"),
    SAVING("Сохранение результата"),
    COMPLETED("Завершено успешно"),
    FAILED("Ошибка обработки");

    private final String description;

    TaskStatus(String description) {
      this.description = description;
    }
  }

  @Override
  public String generateLogFile(String date) {
    String taskId = UUID.randomUUID().toString();
    LogTask task = new LogTask(taskId, TaskStatus.CREATED.name(), date, LocalDateTime.now());
    tasks.put(taskId, task);

    scheduler.schedule(() -> processTask(taskId, date), INITIAL_DELAY_SECONDS, TimeUnit.SECONDS);
    return taskId;
  }

  private void processTask(String taskId, String date) {
    try {
      updateStatus(taskId, TaskStatus.QUEUED);
      updateStatus(taskId, TaskStatus.PREPARING);
      TimeUnit.SECONDS.sleep(PROCESSING_STEP_DELAY_SECONDS);

      updateStatus(taskId, TaskStatus.READING);
      Path logPath = Paths.get(MAIN_LOG_FILE);
      if (!Files.exists(logPath)) {
        throw new IOException("Основной файл логов не найден");
      }

      updateStatus(taskId, TaskStatus.FILTERING);
      TimeUnit.SECONDS.sleep(PROCESSING_STEP_DELAY_SECONDS);
      String filteredLogs = Files.lines(logPath)
              .filter(line -> line.contains(date))
              .collect(Collectors.joining("\n"));

      if (filteredLogs.isEmpty()) {
        throw new IOException("Нет записей за указанную дату");
      }

      updateStatus(taskId, TaskStatus.SAVING);
      TimeUnit.SECONDS.sleep(PROCESSING_STEP_DELAY_SECONDS);
      Path outputFile = Paths.get(LOGS_DIRECTORY)
              .resolve(String.format("logs_%s_%s.log", date, taskId));
      Files.createDirectories(outputFile.getParent());
      Files.write(outputFile, filteredLogs.getBytes());

      updateStatus(taskId, TaskStatus.COMPLETED, outputFile.toString());
    } catch (Exception e) {
      logger.error("Ошибка обработки задачи {}", taskId, e);
      updateStatus(taskId, TaskStatus.FAILED, e.getMessage());
    }
  }

  @Override
  public LogTask getTaskStatus(String taskId) {
    LogTask task = tasks.get(taskId);
    if (task == null) {
      throw new RuntimeException("Задача не найдена");
    }
    return task;
  }

  @Override
  public ResponseEntity<Resource> downloadLogFile(String taskId) {
    LogTask task = tasks.get(taskId);
    if (task == null || !TaskStatus.COMPLETED.name().equals(task.getStatus())) {
      return ResponseEntity.status(423).build(); // 423 Locked
    }

    try {
      Path filePath = Paths.get(task.getFilePath());
      Resource resource = new UrlResource(filePath.toUri());

      return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION,
                      "attachment; filename=" + filePath.getFileName())
              .contentType(MediaType.TEXT_PLAIN)
              .body(resource);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @Override
  public String viewLogsByDate(String date) {
    try {
      Path logPath = Paths.get(MAIN_LOG_FILE);
      if (!Files.exists(logPath)) {
        throw new IOException("Main log file not found");
      }

      try (Stream<String> lines = Files.lines(logPath)) {
        return lines
                .filter(line -> line.contains(date))
                .collect(Collectors.joining("\n"));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error viewing logs", e);
    }
  }

  private void updateStatus(String taskId, TaskStatus status) {
    updateStatus(taskId, status, null);
  }

  private void updateStatus(String taskId, TaskStatus status, String details) {
    LogTask task = tasks.get(taskId);
    if (task != null) {
      task.setStatus(status.name());
      task.setLastUpdated(LocalDateTime.now());
      if (details != null) {
        if (status == TaskStatus.COMPLETED) {
          task.setFilePath(details);
        } else if (status == TaskStatus.FAILED) {
          task.setErrorDetails(details);
        }
      }
      logger.info("{}: {} - {}", taskId, status, status.description);
    }
  }
}
