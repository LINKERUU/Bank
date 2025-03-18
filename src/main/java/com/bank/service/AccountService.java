package com.bank.service;

import com.bank.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service interface for managing {@link Account} entities.
 */
public interface AccountService {

  /**
   * Retrieves all accounts.
   *
   * @return a list of all accounts
   */
  List<Account> findAllAccounts();

  /**
   * Retrieves an account by its ID.
   *
   * @param id the ID of the account to retrieve
   * @return an {@link Optional} containing the account if found, otherwise empty
   */
  Optional<Account> findAccountById(Long id);

  /**
   * Creates a new account.
   *
   * @param account the account to create
   * @return the created account
   */
  Account createAccount(Account account);

  /**
   * Creates multiple accounts in a batch.
   *
   * @param accounts the list of accounts to create
   * @return the list of created accounts
   */
  List<Account> createAccounts(List<Account> accounts);

  /**
   * Updates an existing account.
   *
   * @param id the ID of the account to update
   * @param account the updated account details
   * @return the updated account
   */
  Account updateAccount(Long id, Account account);

  /**
   * Deletes an account by its ID.
   *
   * @param id the ID of the account to delete
   */
  void deleteAccount(Long id);

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   */
  @Transactional
  void deleteCard(Long id);

  /**
   * Retrieves an account by its account number.
   *
   * @param accountNumber the account number to search for
   * @return an {@link Optional} containing the account if found, otherwise empty
   */
  Optional<Account> findByAccountNumber(String accountNumber);
}