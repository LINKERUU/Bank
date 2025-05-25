package com.bank.controller;

import com.bank.model.LogTask;
import com.bank.service.LogService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing log-related operations.
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

  private final LogService logService;

  /**
   * Generates a log file for the given date.
   */
  public LogController(LogService logService) {
    this.logService = logService;
  }

  /**
   * Generates a log file for the given date.
   *
   * @param date the date for which to generate the log file
   * @return a map containing the generated task ID
   */
  @PostMapping("/generate")
  public Map<String, String> generateLogFile(@RequestParam String date) {
    String taskId = logService.generateLogFile(date);
    Map<String, String> response = new HashMap<>();
    response.put("taskId", taskId);
    return response;
  }

  /**
   * Retrieves the status of a specific log generation task.
   *
   * @param taskId the ID of the task
   * @return a map containing the task status and details
   */
  @GetMapping("/status/{taskId}")
  public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
    LogTask task = (LogTask) logService.getTaskStatus(taskId);

    Map<String, Object> response = new HashMap<>();
    response.put("taskId", task.getId());
    response.put("status", task.getStatus());
    response.put("createdAt", task.getCreatedAt());

    if (task.getErrorMessage() != null) {
      response.put("errorMessage", task.getErrorMessage());
    }

    return response;
  }

  /**
   * Downloads the generated log file for the given task ID.
   *
   * @param taskId the ID of the task
   * @return a response entity containing the file
   */
  @GetMapping("/download/{taskId}")
  public ResponseEntity<?> downloadLogFile(@PathVariable String taskId) {
    return logService.downloadLogFile(taskId);
  }

  /**
   * Views logs for a specific date.
   *
   * @param date the date for which to view logs
   * @return logs as a string
   */
  @GetMapping("/view")
  public String viewLogs(@RequestParam String date) {
    return logService.viewLogsByDate(date);
  }
}
