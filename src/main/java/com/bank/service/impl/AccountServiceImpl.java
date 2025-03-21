package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Account;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.service.AccountService;
import com.bank.utils.InMemoryCache;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link AccountService} interface for managing {@link Account} entities.
 */
@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final CardRepository cardRepository;
  private final InMemoryCache<String, List<Account>> accountCache; // Кэш для списка аккаунтов
  private final InMemoryCache<Long, Account> accountByIdCache; // Кэш для аккаунта по ID

  /**
   * Constructs a new AccountServiceImpl with the specified repositories and caches.
   *
   * @param accountRepository the repository for managing accounts
   * @param cardRepository the repository for managing cards
   * @param accountCache the cache for storing lists of accounts
   * @param accountByIdCache the cache for storing accounts by their ID
   */
  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
                            CardRepository cardRepository,
                            InMemoryCache<String, List<Account>> accountCache,
                            InMemoryCache<Long, Account> accountByIdCache) {
    this.accountRepository = accountRepository;
    this.cardRepository = cardRepository;
    this.accountCache = accountCache;
    this.accountByIdCache = accountByIdCache;
  }

  @Override
  public List<Account> findByUserEmail(String email) {
    List<Account> cachedAccounts = accountCache.get(email);
    if (cachedAccounts != null) {
      return cachedAccounts;
    }

    List<Account> accounts = accountRepository.findByUserEmail(email);
    accountCache.put(email, accounts);
    return accounts;
  }

  @Override
  public List<Account> findAllAccounts() {
    String cacheKey = "all_accounts";
    List<Account> cachedAccounts = accountCache.get(cacheKey);
    if (cachedAccounts != null) {
      return cachedAccounts;
    }

    List<Account> accounts = accountRepository.findAll();
    accountCache.put(cacheKey, accounts);
    return accounts;
  }

  @Override
  public Optional<Account> findAccountById(Long id) {
    Account cachedAccount = accountByIdCache.get(id);
    if (cachedAccount != null) {
      return Optional.of(cachedAccount);
    }

    Optional<Account> account = accountRepository.findById(id);
    account.ifPresent(acc -> accountByIdCache.put(id, acc));
    return account;
  }

  @Override
  @Transactional
  public Account createAccount(Account account) {

    if (account.getUsers() == null || account.getUsers().isEmpty()) {
      throw new IllegalArgumentException(
              "Аккаунт должен быть привязан хотя бы к одному пользователю.");
    }

    return accountRepository.save(account);
  }

  @Override
  @Transactional
  public List<Account> createAccounts(List<Account> accounts) {
    return accountRepository.saveAll(accounts);
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

    accountCache.clear();
    accountByIdCache.evict(id);
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

    accountByIdCache.put(id, existingAccount);
    accountCache.clear();

    return accountRepository.save(existingAccount);
  }
}