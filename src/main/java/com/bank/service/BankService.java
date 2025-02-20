package com.bank.service;

import com.bank.model.Account;
import java.util.List;
import org.springframework.stereotype.Service;



/**
 * Service for handling bank account operations.
 */
@Service
public final class BankService {

  /**
   * Default balance for the first account.
   */
  private static final double DEFAULT_BALANCE_1 = 2506.0;

  /**
   * Default balance for the second account.
   */
  private static final double DEFAULT_BALANCE_2 = 306.0;

  /**
   * List of bank accounts.
   */
  private final List<Account> accounts = List.of(
          new Account(1, "Gerald", DEFAULT_BALANCE_1),
          new Account(2, "Anna", DEFAULT_BALANCE_2)
  );

  /**
   * Retrieve a list of all accounts.
   *
   * @return list of accounts
   */
  public List<Account> findAllAccounts() {
    return accounts;
  }

  /**
   * Find an account by owner name.
   *
   * @param ownerName owner's name
   * @return account
   */
  public Account getAccountByOwnerName(final String ownerName) {
    return accounts.stream()
            .filter(account -> account
                    .getOwnerName()
                    .equalsIgnoreCase(ownerName))
            .findFirst()
            .orElse(null);
  }

  /**
   * Find an account by ID.
   *
   * @param accountId account identifier
   * @return account
   */
  public Account getAccountById(final int accountId) {
    return accounts.stream()
            .filter(account -> account.getAccountId() == accountId)
            .findFirst()
            .orElse(null);
  }
}
