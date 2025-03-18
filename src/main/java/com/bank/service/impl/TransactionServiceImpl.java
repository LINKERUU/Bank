package com.bank.service.impl;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.TransactionService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link TransactionService} interface for managing transactions.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  /**
   * Constructs a new TransactionServiceImpl with the specified repositories.
   *
   * @param transactionRepository the repository for transaction operations
   * @param accountRepository the repository for account operations
   */
  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  public List<Transaction> findAllTransactions() {
    return transactionRepository.findAll();
  }

  @Override
  public Optional<Transaction> findTransactionById(Long id) {
    return transactionRepository.findById(id);
  }

  @Override
  @Transactional
  public Transaction createTransaction(Transaction transaction) {
    // Find the account by accountId
    Account account = accountRepository.findById(transaction.getAccountId())
            .orElseThrow(() -> new RuntimeException("Учётная запись с ID "
                    + transaction.getAccountId() + " не найдена"));

    // Set the account in the transaction
    transaction.setAccount(account);

    // Save the transaction
    return transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
    // Находим существующую транзакцию по ID
    Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Транзакция с ID " + id + " не найдена"));

    // Находим счет, связанный с существующей транзакцией
    Account account = existingTransaction.getAccount();

    // Откатываем старую транзакцию: возвращаем сумму на счет
    if ("debit".equalsIgnoreCase(existingTransaction.getTransactionType())) {
      // Если это дебетовая транзакция, вычитаем сумму из баланса (возвращаем обратно)
      account.setBalance(account.getBalance() - existingTransaction.getAmount());
    } else if ("credit".equalsIgnoreCase(existingTransaction.getTransactionType())) {
      // Если это кредитовая транзакция, добавляем сумму к балансу (возвращаем обратно)
      account.setBalance(account.getBalance() + existingTransaction.getAmount());
    }

    // Применяем новую транзакцию: обновляем баланс счета на новую сумму
    if (updatedTransaction.getAmount() != null) {
      if ("debit".equalsIgnoreCase(updatedTransaction.getTransactionType())) {
        // Если это дебетовая транзакция, добавляем новую сумму к балансу
        account.setBalance(account.getBalance() + updatedTransaction.getAmount());
      } else if ("credit".equalsIgnoreCase(updatedTransaction.getTransactionType())) {
        // Если это кредитовая транзакция, вычитаем новую сумму из баланса
        if (account.getBalance() < updatedTransaction.getAmount()) {
          throw new IllegalArgumentException("Недостаточно средств на счете!");
        }
        account.setBalance(account.getBalance() - updatedTransaction.getAmount());
      }
    }

    // Сохраняем обновленный баланс счета
    accountRepository.save(account);

    // Обновляем поля транзакции
    if (updatedTransaction.getAmount() != null) {
      existingTransaction.setAmount(updatedTransaction.getAmount());
    }
    if (updatedTransaction.getTransactionType() != null) {
      existingTransaction.setTransactionType(updatedTransaction.getTransactionType());
    }
    if (updatedTransaction.getDescription() != null) {
      existingTransaction.setDescription(updatedTransaction.getDescription());
    }

    // Обновляем счет, если accountId изменен
    if (updatedTransaction.getAccountId() != null) {
      Account newAccount = accountRepository.findById(updatedTransaction.getAccountId())
              .orElseThrow(() -> new RuntimeException("Учётная запись с ID "
                      + updatedTransaction.getAccountId() + " не найдена"));
      existingTransaction.setAccount(newAccount);
    } else {
      // Если accountId не предоставлен, оставляем текущий счет
      existingTransaction.setAccount(existingTransaction.getAccount());
    }

    // Сохраняем обновленную транзакцию
    return transactionRepository.save(existingTransaction);
  }


  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    // Находим транзакцию по ID
    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Транзакция с ID " + id + " не найдена"));

    // Находим счет, связанный с транзакцией
    Account account = transaction.getAccount();

    // Возвращаем сумму транзакции на счет
    if ("credit".equalsIgnoreCase(transaction.getTransactionType())) {
      // Если это дебетовая транзакция, добавляем сумму обратно на счет
      account.setBalance(account.getBalance() + transaction.getAmount());
    } else if ("debit".equalsIgnoreCase(transaction.getTransactionType())) {
      // Если это кредитовая транзакция, вычитаем сумму из счета
      account.setBalance(account.getBalance() - transaction.getAmount());
    }

    // Сохраняем обновленный баланс счета
    accountRepository.save(account);

    // Удаляем транзакцию
    transactionRepository.deleteById(id);
  }
}