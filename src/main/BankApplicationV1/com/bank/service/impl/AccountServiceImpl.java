package com.bank.service.impl;

import com.bank.model.Account;
import com.bank.service.AccountService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса для управления банковскими счетами.
 */
@Service
public class AccountServiceImpl implements AccountService {

  /**
   * Стандартный баланс для первого аккаунта.
   */
  private static final double DEFAULT_BALANCE_1 = 2506.0;

  /**
   * Стандартный баланс для второго аккаунта.
   */
  private static final double DEFAULT_BALANCE_2 = 306.0;

  /**
   * Список банковских счетов.
   */
  private final List<Account> accounts = List.of(
          new Account(1, "Gerald", DEFAULT_BALANCE_1),
          new Account(2, "Anna", DEFAULT_BALANCE_2)
  );

  /**
   * Возвращает список всех банковских счетов.
   *
   * @return список счетов
   */
  @Override
  public List<Account> findAllAccounts() {
    return accounts;
  }

  /**
   * Находит банковский счет по имени владельца.
   *
   * @param ownerName имя владельца
   * @return найденный счет или {@code null}, если не найден
   */
  @Override
  public Account getAccountByOwnerName(String ownerName) {
    return accounts.stream()
            .filter(account -> account.getOwnerName().equalsIgnoreCase(ownerName))
            .findFirst()
            .orElse(null);
  }

  /**
   * Находит банковский счет по его идентификатору.
   *
   * @param id идентификатор счета
   * @return найденный счет или {@code null}, если не найден
   */
  @Override
  public Account getAccountById(int id) {
    return accounts.stream()
            .filter(account -> account.getId() == id)
            .findFirst()
            .orElse(null);
  }
}
