package com.bank.serviceImpl.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.serviceImpl.AccountService;
import com.bank.utils.InMemoryCache;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AccountService} that provides business logic
 * for managing bank accounts with caching support.
 */
@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final CardRepository cardRepository;
  private final InMemoryCache<Long, Account> accountCache;

  /**
   * Constructs an AccountServiceImpl with required dependencies.
   *
   * @param accountRepository the account repository
   * @param cardRepository the card repository
   * @param accountCache the in-memory cache for accounts
   */
  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
                            CardRepository cardRepository,
                            InMemoryCache<Long, Account> accountCache) {
    this.accountRepository = accountRepository;
    this.cardRepository = cardRepository;
    this.accountCache = accountCache;
  }

  @Override
  public List<Account> findByUserEmail(String email) {
    return accountRepository.findByUserEmail(email);
  }

  @Override
  public  List<Account> findAccountsWithCards() {
    return accountRepository.findAccountsWithCards();
  }

  @Override
  public List<Account> findAllAccounts() {
    // Не кэшируем список, так как он часто меняется
    return accountRepository.findAll();
  }

  @Override
  public Optional<Account> findAccountById(Long id) {
    Account cachedAccount = accountCache.get(id);
    if (cachedAccount != null) {
      return Optional.of(cachedAccount);
    }

    Optional<Account> account = accountRepository.findById(id);
    account.ifPresent(acc -> accountCache.put(id, acc));
    return account;
  }

  @Override
  @Transactional
  public Account createAccount(Account account) {
    // Валидация данных
    if (account.getAccountNumber() == null || account.getAccountNumber().length() < 10
            || account.getAccountNumber().length() > 20) {
      throw new ValidationException("The account number must"
              + " be between 10 and 20 characters long.");
    }
    if (!account.getAccountNumber().matches("^[0-9]+$")) {
      throw new ValidationException("The account number must contain only numbers.");
    }
    if (account.getBalance() == null || account.getBalance() < 0) {
      throw new ValidationException("The balance cannot be negative");
    }
    if (account.getUsers() == null || account.getUsers().isEmpty()) {
      throw new ValidationException("The account must be linked to at least one user.");
    }

    Account savedAccount = accountRepository.save(account);
    accountCache.put(savedAccount.getId(), savedAccount);
    return savedAccount;
  }

  @Override
  @Transactional
  public List<Account> createAccounts(List<Account> accounts) {
    // Валидация всех счетов перед сохранением
    List<String> validationErrors = accounts.stream()
            .map(account -> {
              try {
                validateAccount(account);
                return null;
              } catch (ValidationException e) {
                return "Account " + account.getAccountNumber() + ": " + e.getMessage();
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Batch validation failed:\n" +
              String.join("\n", validationErrors));
    }

    // Сохранение и кэширование
    return accounts.stream()
            .map(accountRepository::save)
            .peek(account -> accountCache.put(account.getId(), account))
            .collect(Collectors.toList());
  }

  private void validateAccount(Account account) {
    if (account.getAccountNumber() == null || account.getAccountNumber().length() < 10
            || account.getAccountNumber().length() > 20) {
      throw new ValidationException("Account number must be between 10 and 20 characters");
    }
    if (!account.getAccountNumber().matches("^\\d+$")) {
      throw new ValidationException("Account number must contain only digits");
    }
    if (account.getBalance() == null || account.getBalance() < 0) {
      throw new ValidationException("Balance cannot be negative");
    }
  }

  @Override
  @Transactional
  public void deleteAccount(Long id) {
    Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Аккаунт с ID " + id + " не найден."));

    if (account.getCards() != null && !account.getCards().isEmpty()) {
      cardRepository.deleteAll(account.getCards());
    }

    accountRepository.delete(account);
    accountCache.evict(id);
  }

  @Override
  @Transactional
  public void deleteAccounts(List<Long> ids) {
    ids.forEach(id -> {
      Account account = accountRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

      // Удаляем связанные карты
      if (account.getCards() != null && !account.getCards().isEmpty()) {
        cardRepository.deleteAll(account.getCards());
      }

      accountRepository.delete(account);
      accountCache.evict(id);
    });
  }

  @Override
  @Transactional
  public Account updateAccount(Long id, Account updatedAccount) {
    Account existingAccount = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Аккаунт с ID " + id + " не найден."));

    if (updatedAccount.getAccountNumber() != null) {
      existingAccount.setAccountNumber(updatedAccount.getAccountNumber());
    }
    if (updatedAccount.getBalance() != null) {
      existingAccount.setBalance(updatedAccount.getBalance());
    }

    Account savedAccount = accountRepository.save(existingAccount);
    accountCache.put(id, savedAccount);
    return savedAccount;
  }

  @Override
  @Transactional
  public List<Account> updateAccounts(List<Account> accounts) {
    return accounts.stream()
            .peek(account -> {
              if (account.getId() == null) {
                throw new ValidationException("Account ID cannot be null for update");
              }
            })
            .map(account -> {
              Account existing = accountRepository.findById(account.getId())
                      .orElseThrow(() -> new ResourceNotFoundException(
                              "Account not found with id: " + account.getId()));

              // Обновляем только разрешенные поля
              if (account.getBalance() != null) {
                existing.setBalance(account.getBalance());
              }
              return existing;
            })
            .map(accountRepository::save)
            .peek(account -> accountCache.put(account.getId(), account))
            .collect(Collectors.toList());
  }
}