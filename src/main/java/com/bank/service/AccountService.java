package com.bank.service;

import com.bank.dto.AccountDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing bank accounts.
 * Provides methods for creating, reading, updating, and deleting accounts,
 * as well as various account-related operations.
 */
public interface AccountService {

  /**
   * Updates users associated with an account.
   *
   * @param accountId the ID of the account to update
   * @param userIds set of user IDs to associate with the account
   * @return updated account DTO
   */
  AccountDto updateAccountUsers(Long accountId, Set<Long> userIds);

  /**
   * Finds accounts by user email.
   *
   * @param email the email of the user
   * @return list of account DTOs associated with the user
   */
  List<AccountDto> findByUserEmail(String email);

  /**
   * Finds accounts that have cards associated with them.
   *
   * @return list of account DTOs with cards
   */
  List<AccountDto> findAccountsWithCards();

  /**
   * Retrieves all accounts.
   *
   * @return list of all account DTOs
   */
  List<AccountDto> findAllAccounts();

  /**
   * Finds an account by its ID.
   *
   * @param id the account ID
   * @return Optional containing the account DTO if found
   */
  Optional<AccountDto> findAccountById(Long id);

  /**
   * Creates a new account.
   *
   * @param accountDto the account data to create
   * @return created account DTO
   * @throws ValidationException if the account data is invalid
   */
  AccountDto createAccount(AccountDto accountDto) throws ValidationException;

  /**
   * Creates multiple accounts in batch.
   *
   * @param accountDtos list of account data to create
   * @return list of created account DTOs
   * @throws ValidationException if any account data is invalid
   */
  List<AccountDto> createAccounts(List<AccountDto> accountDtos) throws ValidationException;

  /**
   * Deletes an account by its ID.
   *
   * @param id the account ID to delete
   * @throws ResourceNotFoundException if the account is not found
   */
  void deleteAccount(Long id) throws ResourceNotFoundException;

  /**
   * Updates an existing account.
   *
   * @param id the ID of the account to update
   * @param accountDto the updated account data
   * @return updated account DTO
   * @throws ResourceNotFoundException if the account is not found
   */
  AccountDto updateAccount(Long id, AccountDto accountDto) throws ResourceNotFoundException;
}