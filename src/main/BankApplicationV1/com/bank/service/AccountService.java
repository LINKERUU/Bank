package com.bank.service;

import com.bank.model.Account;
import java.util.List;

/**
 * Интерфейс для сервиса управления банковскими счетами.
 */
public interface AccountService {

  /**
   * Получает список всех счетов.
   *
   * @return список счетов
   */
  List<Account> findAllAccounts();

  /**
   * Ищет банковский счет по имени владельца.
   *
   * @param ownerName имя владельца
   * @return банковский счет или {@code null}, если не найден
   */
  Account getAccountByOwnerName(String ownerName);

  /**
   * Ищет банковский счет по его идентификатору.
   *
   * @param id идентификатор счета
   * @return банковский счет или {@code null}, если не найден
   */
  Account getAccountById(int id);
}
