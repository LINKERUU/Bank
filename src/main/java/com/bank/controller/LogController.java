package com.bank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling log file operations.
 * Provides endpoints for downloading and viewing log files filtered by date.
 */
@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Controller", description = "API для работы с лог-файлами")
public class LogController {

  private static final String LOG_FILE_PATH = "./bank.log";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Downloads log file filtered by specified date.
   *
   * @param dateStr the date string in yyyy-MM-dd format to filter logs
   * @return ResponseEntity containing the log file as a downloadable resource
   * @throws IOException if there's an error reading the log file
   */
  @GetMapping("/download")
  @Operation(summary = "Скачать лог-файл", description = "Скачивает логи за указанную дату.")
  @ApiResponse(responseCode = "200", description = "Логи успешно загружены")
  @ApiResponse(responseCode = "404", description = "Логи не найдены")
  @ApiResponse(responseCode = "400", description = "Неверный формат даты")
  public ResponseEntity<Resource> downloadLogFile(
          @Parameter(description = "Дата в формате **yyyy-MM-dd**",
                  required = true, example = "2023-10-01")
          @RequestParam(name = "date") String dateStr) throws IOException {

    // Validate date format
    try {
      LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest().build();
    }

    // Check if file exists
    Path logPath = Paths.get(LOG_FILE_PATH);
    if (!Files.exists(logPath)) {
      return ResponseEntity.notFound().build();
    }

    // Read and filter logs
    String filteredLogs;
    try (var lines = Files.lines(logPath)) {
      filteredLogs = lines
              .filter(line -> line.contains(dateStr))
              .collect(Collectors.joining("\n"));
    }

    if (filteredLogs.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    // Create temp file for download with safe naming
    Path tempLogFile;
    try {
      // Generate random filename prefix instead of using user input
      String randomPrefix = "bank-logs-" + UUID.randomUUID().toString();
      tempLogFile = Files.createTempFile(randomPrefix, ".log");

      // Write filtered logs to the temp file
      Files.writeString(tempLogFile, filteredLogs);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }

    Resource resource = new UrlResource(tempLogFile.toUri());
    tempLogFile.toFile().deleteOnExit();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=logs-" + dateStr + ".log")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
  }

  /**
   * Views log content filtered by specified date.
   *
   * @param dateStr the date string in yyyy-MM-dd format to filter logs
   * @return ResponseEntity containing the filtered logs as plain text
   * @throws IOException if there's an error reading the log file
   */
  @GetMapping("/view")
  @Operation(summary = "Просмотреть логи",
          description = "Возвращает логи за указанную дату в виде текста.")
  @ApiResponse(responseCode = "200", description = "Логи успешно получены")
  @ApiResponse(responseCode = "404", description = "Логи не найдены")
  @ApiResponse(responseCode = "400", description = "Неверный формат даты")
  public ResponseEntity<String> viewLogFile(
          @Parameter(description = "Дата в формате **yyyy-MM-dd**",
                  required = true, example = "2023-10-01")
          @RequestParam(name = "date") String dateStr) throws IOException {

    // Проверка формата даты
    try {
      LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest().build();
    }

    // Проверка существования файла
    Path logPath = Paths.get(LOG_FILE_PATH);
    if (!Files.exists(logPath)) {
      return ResponseEntity.notFound().build();
    }

    // Фильтрация логов по дате с использованием try-with-resources
    String filteredLogs;
    try (Stream<String> lines = Files.lines(logPath)) {
      filteredLogs = lines
              .filter(line -> line.contains(dateStr))
              .collect(Collectors.joining("\n"));
    }

    if (filteredLogs.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(filteredLogs);
  }
}