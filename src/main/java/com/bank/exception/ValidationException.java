package com.bank.exception;

/**
 * Custom exception class for validation errors in the banking application.
 * This exception is thrown when business rule validations fail.
 */
public class ValidationException extends RuntimeException {

  /**
   * Constructs a new validation exception with the specified detail message.
   *
   * @param message the detail message describing the validation failure
   */
  public ValidationException(String message) {
    super(message);
  }
}