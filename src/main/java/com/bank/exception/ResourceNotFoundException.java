package com.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при отсутствии запрошенной сущности.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Создает новое исключение с указанным сообщением.
   *
   * @param message сообщение, описывающее причину исключения
   */
  public ResourceNotFoundException(final String message) {
    super(message);
  }
}