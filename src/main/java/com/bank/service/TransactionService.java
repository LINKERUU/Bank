package com.bank.service;

import com.bank.dto.TransactionDto;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления транзакциями.
 */
public interface TransactionService {

  /**
   * Получает список всех транзакций.
   *
   * @return список DTO транзакций
   */
  List<TransactionDto> findAllTransactions();

  /**
   * Получает транзакцию по её ID.
   *
   * @param id идентификатор транзакции
   * @return Optional с DTO транзакции, если найдена
   */
  Optional<TransactionDto> findTransactionById(Long id);

  /**
   * Создаёт новую транзакцию.
   *
   * @param transactionDto DTO создаваемой транзакции
   * @return созданная транзакция
   */
  TransactionDto createTransaction(TransactionDto transactionDto);

  /**
   * Обновляет существующую транзакцию.
   *
   * @param id идентификатор обновляемой транзакции
   * @param transactionDto DTO с обновлёнными данными
   * @return обновлённая транзакция
   */
  TransactionDto updateTransaction(Long id, TransactionDto transactionDto);

  /**
   * Удаляет транзакцию по ID.
   *
   * @param id идентификатор транзакции
   */
  void deleteTransaction(Long id);
}
