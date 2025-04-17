package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.service.AccountService;
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
  public Account createAccount(Account account) {
    // Валидация
    if (account.getUsers() == null || account.getUsers().isEmpty()) {
      throw new ValidationException("The account must be linked to at least one user");
    }

    // Другие проверки...
    validateAccount(account);

    Account savedAccount = accountRepository.save(account);
    accountCache.put(savedAccount.getId(), savedAccount);
    return savedAccount;
  }

  @Override
  @Transactional
  public List<Account> createAccounts(List<Account> accounts) {
    // Validate all accounts
    List<String> validationErrors = accounts.stream()
            .map(account -> {
              try {
                validateAccount(account);
                if (account.getUsers() == null || account.getUsers().isEmpty()) {
                  return "Account " + account.getAccountNumber()
                          + ": must be linked to at least one user";
                }
                return null;
              } catch (ValidationException e) {
                return "Account " + account.getAccountNumber() + ": " + e.getMessage();
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Batch validation failed:\n"
              + String.join("\n", validationErrors));
    }

    List<Account> savedAccounts = accountRepository.saveAll(accounts);
    savedAccounts.forEach(account -> accountCache.put(account.getId(), account));
    return savedAccounts;
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
    // Validate input
    if (accounts == null) {
      throw new ValidationException("Accounts list cannot be null");
    }

    // Check for null accounts
    if (accounts.stream().anyMatch(Objects::isNull)) {
      throw new ValidationException("Account list contains null elements");
    }

    // Validate each account
    accounts.forEach(this::validateAccount);

    List<Account> updatedAccounts = accountRepository.saveAll(accounts);

    // Cache the updated accounts
    updatedAccounts.forEach(account ->
            accountCache.put(account.getId(), account));

    return updatedAccounts;
  }
}