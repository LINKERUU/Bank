package com.bank.service;

import com.bank.model.LogTask;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

/**
 * Service interface for managing log-related operations.
 */
public interface LogService {

  /**
   * Initiates the generation of a log file for the specified date.
   *
   * @param date the date for which the log file is to be generated
   * @return the task ID of the log generation request
   */
  String generateLogFile(String date);

  /**
   * Retrieves the status of a specific log generation task.
   *
   * @param taskId the ID of the task
   * @return the {@link LogTask} containing task details
   */
  LogTask getTaskStatus(String taskId);

  /**
   * Downloads the generated log file for the given task ID.
   *
   * @param taskId the ID of the task
   * @return a response entity containing the log file as a {@link Resource}
   */
  ResponseEntity<Resource> downloadLogFile(String taskId);

  /**
   * Returns the log content for a specified date.
   *
   * @param date the date for which to retrieve logs
   * @return the log content as a string
   */
  String viewLogsByDate(String date);
}
