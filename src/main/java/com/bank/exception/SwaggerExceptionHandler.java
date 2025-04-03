package com.bank.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception handler specifically for Swagger-related exceptions.
 * This handler has higher priority (Order=1) to intercept Swagger exceptions
 * before they reach the global exception handler.
 */
@ControllerAdvice
@Order(1) // Более высокий приоритет
public class SwaggerExceptionHandler {

  /**
   * Handles exceptions related to Swagger documentation.
   * Silently ignores Swagger-specific exceptions by returning HTTP 200 OK,
   * allowing other exceptions to be processed by lower-priority handlers.
   *
   * @param ex the caught exception
   * @return ResponseEntity with OK status for Swagger errors,
   *         or null to allow other handlers to process non-Swagger exceptions
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> handleSwaggerException(Exception ex) {
    if (ex.getMessage() != null && ex.getMessage().contains("org.springdoc")) {
      return new ResponseEntity<>("Swagger error ignored", HttpStatus.OK);
    }
    return null; // Пропускаем обработку, если ошибка не связана со Swagger
  }
}