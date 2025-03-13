package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.service.AccountService;
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

  /**
   * Constructor.
   */
  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public List<Account> findAllAccounts() {
    return accountRepository.findAll();
  }

  @Override
  public Optional<Account> findAccountById(Long id) {
    return accountRepository.findById(id);
  }

  @Override
  @Transactional
  public Account createAccount(Account account) {
    return accountRepository.save(account);
  }

  @Override
  @Transactional
  public List<Account> createAccounts(List<Account> accounts) {
    return accountRepository.saveAll(accounts);
  }

  @Override
  @Transactional
  public Account updateAccount(Long id, Account account) {
    account.setId(id); // Убедимся, что ID обновляемого счета совпадает с переданным ID
    return accountRepository.save(account);
  }

  @Override
  @Transactional
  public void deleteAccount(Long id) {
    Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Не найден аккаунт с id: " + id));

    // Удаляем связи с пользователями
    for (User user : account.getUsers()) {
      user.getAccounts().remove(account); // Удаляем аккаунт из списка пользователя
    }
    account.getUsers().clear(); // Очищаем список пользователей у аккаунта

    // Удаляем аккаунт
    accountRepository.delete(account);
  }

  @Override
  public Optional<Account> findByAccountNumber(String accountNumber) {
    return accountRepository.findByAccountNumber(accountNumber);
  }
}