package com.bank.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a task that handles log processing.
 */
@Getter
public class LogTask {

  private final String id;

  @Setter
  private String status;

  private final String date;

  @Setter
  private String filePath;

  @Setter
  private String errorMessage;

  private final LocalDateTime createdAt;

  /**
   * Constructs a new LogTask.
   *
   * @param id        the unique task identifier
   * @param status    the initial task status
   * @param date      the date for which logs are being processed
   * @param createdAt the time the task was created
   */
  public LogTask(String id, String status, String date, LocalDateTime createdAt) {
    this.id = id;
    this.status = status;
    this.date = date;
    this.createdAt = createdAt;
  }

  /**
   * Sets the last updated timestamp for this task.
   *
   * @param now the timestamp to set
   */
  public void setLastUpdated(LocalDateTime now) {
    // Implement logic if needed
  }

  /**
   * Sets the error details for this task.
   *
   * @param details the error message
   */
  public void setErrorDetails(String details) {
    // Implement logic if needed
  }
}
