package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Account;
import com.bank.model.Card;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
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
  private final CardRepository cardRepository;

  /**
   * Constructor.
   */
  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository, CardRepository cardRepository) {
    this.accountRepository = accountRepository;
    this.cardRepository = cardRepository;
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

    return accountRepository.save(existingAccount);
  }




  @Override
  @Transactional
  public void deleteCard(Long id) {
    Card card = cardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Не найдена карта с id: " + id));

    // Удаляем карту из списка карт аккаунта
    Account account = card.getAccount();
    if (account != null) {
      account.getCards().remove(card);
    }

    // Удаляем саму карту
    cardRepository.delete(card);
  }



  @Override
  public Optional<Account> findByAccountNumber(String accountNumber) {
    return accountRepository.findByAccountNumber(accountNumber);
  }
}