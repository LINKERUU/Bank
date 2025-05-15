package com.bank.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class LogTask {
  // Геттеры
  private final String id;
  // Сеттеры
  @Setter
  private String status;
  private final String date;
  @Setter
  private String filePath;
  @Setter
  private String errorMessage;
  private final LocalDateTime createdAt;

  public LogTask(String id, String status, String date, LocalDateTime createdAt) {
    this.id = id;
    this.status = status;
    this.date = date;
    this.createdAt = createdAt;
  }

  public void setLastUpdated(LocalDateTime now) {
  }

  public void setErrorDetails(String details) {
  }
}