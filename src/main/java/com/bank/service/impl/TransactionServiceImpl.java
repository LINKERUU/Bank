package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
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
 * Service implementation for managing financial transactions.
 * Provides methods for creating, reading, updating, and deleting transactions
 * while maintaining account balance consistency.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private static final String TRANSACTION_TYPE_DEBIT = "debit";
  private static final String TRANSACTION_TYPE_CREDIT = "credit";

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  /**
   * Constructs a TransactionServiceImpl with required repositories.
   *
   * @param transactionRepository repository for transaction data access
   * @param accountRepository repository for account data access
   */
  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  @Override
  @Transactional(readOnly = true)
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
            .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: "
                    + transaction.getAccountId()));

    transaction.setAccount(account);
    return transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
    Transaction existing = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: "
                    + id));

    updateAccountBalance(existing, updatedTransaction);
    updateTransactionFields(existing, updatedTransaction);

    return transactionRepository.save(existing);
  }

  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: "
                    + id));

    revertAccountBalance(transaction);
    transactionRepository.deleteById(id);
  }

  private void updateAccountBalance(Transaction existing, Transaction updated) {
    Account account = existing.getAccount();

    // Revert existing transaction effect
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(existing.getTransactionType())) {
      account.setBalance(account.getBalance() + existing.getAmount());
    } else if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(existing.getTransactionType())) {
      account.setBalance(account.getBalance() - existing.getAmount());
    }

    // Apply new transaction effect
    if (updated.getAmount() != null && updated.getTransactionType() != null) {
      if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(updated.getTransactionType())) {
        if (account.getBalance() < updated.getAmount()) {
          throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - updated.getAmount());
      } else if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(updated.getTransactionType())) {
        account.setBalance(account.getBalance() + updated.getAmount());
      }
    }

    accountRepository.save(account);
  }

  private void updateTransactionFields(Transaction existing, Transaction updated) {
    if (updated.getAmount() != null) {
      existing.setAmount(updated.getAmount());
    }
    if (updated.getTransactionType() != null) {
      existing.setTransactionType(updated.getTransactionType());
    }
    if (updated.getDescription() != null) {
      existing.setDescription(updated.getDescription());
    }
    if (updated.getAccountId() != null) {
      Account newAccount = accountRepository.findById(updated.getAccountId())
              .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
      existing.setAccount(newAccount);
    }
  }

  private void revertAccountBalance(Transaction transaction) {
    Account account = transaction.getAccount();

    if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transaction.getTransactionType())) {
      if (account.getBalance() < transaction.getAmount()) {
        throw new IllegalArgumentException("Cannot revert transaction - insufficient funds");
      }
      account.setBalance(account.getBalance() - transaction.getAmount());
    } else if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())) {
      account.setBalance(account.getBalance() + transaction.getAmount());
    }

    accountRepository.save(account);
  }
}