package com.bank.exception;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for REST controllers.
 * Handles various types of exceptions and returns appropriate HTTP responses.
 */
@ControllerAdvice
@Order(0) // Устанавливаем порядок обработки
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // Обработка ошибок валидации (400)
  /**
   * Handles validation exceptions for method arguments.
   *
   * @param ex the MethodArgumentNotValidException that was thrown
   * @return ResponseEntity containing a map of field errors with BAD_REQUEST status
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>>
      handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    // Дотируем ошибку с уровнем ERROR
    logger.error("Validation error: {}", errors);

    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles custom validation exceptions.
   *
   * @param ex the ValidationException that was thrown
   * @return ResponseEntity containing the error message with BAD_REQUEST status
   */
  // Обработка кас томного исключения ValidationException
  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleValidationException(ValidationException ex) {
    // Дотируем ошибку с уровнем ERROR
    logger.error("Validation error: {}", ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  // Обработка других исключений (500)
  /**
   * Handles all other unexpected exceptions.
   *
   * @param ex the caught Exception
   * @return ResponseEntity containing error message with INTERNAL_SERVER_ERROR status,
   *         except for Swagger-related errors which return OK status
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<String> handleGenericException(Exception ex) {
    // Игнорируем ошибки, связанные с Swagger
    if (ex.getMessage() != null && ex.getMessage().contains("org.springdoc")) {
      return new ResponseEntity<>("Swagger error ignored", HttpStatus.OK);
    }

    // Дотируем ошибку с уровнем ERROR
    logger.error("Internal server error: {}", ex.getMessage(), ex);
    return new ResponseEntity<>("An error occurred: "
            + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}