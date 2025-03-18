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

  private static final String NOT_FOUND_MESSAGE = " не найдена";
  private static final String TRANSACTION_TYPE_DEBIT = "debit";
  private static final String TRANSACTION_TYPE_CREDIT = "credit";

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
    Account account = accountRepository.findById(transaction.getAccountId())
            .orElseThrow(() -> new RuntimeException("Учётная запись с ID "
                    + transaction.getAccountId() + NOT_FOUND_MESSAGE));

    transaction.setAccount(account);

    return transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
    Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Транзакция с ID " + id + NOT_FOUND_MESSAGE));

    Account account = getAccount(updatedTransaction, existingTransaction);

    accountRepository.save(account);

    if (updatedTransaction.getAmount() != null) {
      existingTransaction.setAmount(updatedTransaction.getAmount());
    }
    if (updatedTransaction.getTransactionType() != null) {
      existingTransaction.setTransactionType(updatedTransaction.getTransactionType());
    }
    if (updatedTransaction.getDescription() != null) {
      existingTransaction.setDescription(updatedTransaction.getDescription());
    }

    if (updatedTransaction.getAccountId() != null) {
      Account newAccount = accountRepository.findById(updatedTransaction.getAccountId())
              .orElseThrow(() -> new RuntimeException("Учётная запись с ID "
                      + updatedTransaction.getAccountId() + NOT_FOUND_MESSAGE));
      existingTransaction.setAccount(newAccount);
    } else {

      existingTransaction.setAccount(existingTransaction.getAccount());
    }

    return transactionRepository.save(existingTransaction);
  }

  private static Account getAccount(Transaction updatedTransaction,
                                    Transaction existingTransaction) {
    Account account = existingTransaction.getAccount();

    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(existingTransaction.getTransactionType())) {

      account.setBalance(account.getBalance() - existingTransaction.getAmount());
    } else if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(existingTransaction.getTransactionType())) {

      account.setBalance(account.getBalance() + existingTransaction.getAmount());
    }


    if (updatedTransaction.getAmount() != null) {
      if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(updatedTransaction.getTransactionType())) {

        account.setBalance(account.getBalance() + updatedTransaction.getAmount());
      } else if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(updatedTransaction
              .getTransactionType())) {

        if (account.getBalance() < updatedTransaction.getAmount()) {
          throw new IllegalArgumentException("Недостаточно средств на счете!");
        }
        account.setBalance(account.getBalance() - updatedTransaction.getAmount());
      }
    }
    return account;
  }

  @Override
  @Transactional
  public void deleteTransaction(Long id) {

    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Транзакция с ID " + id + NOT_FOUND_MESSAGE));


    Account account = transaction.getAccount();


    if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transaction.getTransactionType())) {

      account.setBalance(account.getBalance() + transaction.getAmount());
    } else if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())) {

      account.setBalance(account.getBalance() - transaction.getAmount());
    }

    accountRepository.save(account);

    transactionRepository.deleteById(id);
  }
}