package com.bank.serviceImpl.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.serviceImpl.TransactionService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the TransactionService interface.
 * Provides business logic for transaction operations including
 * creation, retrieval, updating and deletion of transactions.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private static final String TRANSACTION_TYPE_DEBIT = "debit";
  private static final String TRANSACTION_TYPE_CREDIT = "credit";

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  /**
   * Constructs a new TransactionServiceImpl with required repositories.
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
  @Transactional(readOnly = true)
  public Optional<Transaction> findTransactionById(Long id) {
    if (id == null || id <= 0) {
      throw new ValidationException("Invalid transaction ID");
    }
    return transactionRepository.findById(id);
  }

  @Override
  @Transactional
  public Transaction createTransaction(Transaction transaction) {
    validateTransaction(transaction);

    Account account = accountRepository.findById(transaction.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account not found with ID: " + transaction.getAccountId()));

    processTransaction(transaction, account);

    accountRepository.save(account);
    return transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
    validateTransactionId(id);
    validateTransaction(updatedTransaction);

    Transaction existing = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with ID: " + id));

    updateAccountBalance(existing, updatedTransaction);
    updateTransactionFields(existing, updatedTransaction);

    return transactionRepository.save(existing);
  }

  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    validateTransactionId(id);

    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with ID: " + id));

    revertAccountBalance(transaction);
    transactionRepository.deleteById(id);
  }

  private void validateTransactionId(Long id) {
    if (id == null || id <= 0) {
      throw new ValidationException("Invalid transaction ID");
    }
  }

  private void validateTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new ValidationException("Transaction cannot be null");
    }

    if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
      throw new ValidationException("Transaction amount must be positive");
    }

    if (transaction.getTransactionType() == null
            || !(TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())
            || TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transaction.getTransactionType()))) {
      throw new ValidationException("Transaction type must be 'credit' or 'debit'");
    }

    if (transaction.getAccountId() == null) {
      throw new ValidationException("Account ID cannot be null");
    }
  }

  private void processTransaction(Transaction transaction, Account account) {
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())) {

      account.setBalance(account.getBalance() + transaction.getAmount());
    } else {
      if (account.getBalance() < transaction.getAmount()) {
        throw new ValidationException("Insufficient funds for debit transaction");
      }
      account.setBalance(account.getBalance() - transaction.getAmount());
    }
    transaction.setAccount(account);
  }

  private void updateAccountBalance(Transaction existing, Transaction updated) {
    Account account = existing.getAccount();

    // Revert existing transaction effect
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(existing.getTransactionType())) {
      account.setBalance(account.getBalance() - existing.getAmount());
    } else {
      account.setBalance(account.getBalance() + existing.getAmount());
    }

    // Apply new transaction effect
    if (updated.getAmount() != null && updated.getTransactionType() != null) {
      if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(updated.getTransactionType())) {
        account.setBalance(account.getBalance() + updated.getAmount());
      } else {
        if (account.getBalance() < updated.getAmount()) {
          throw new ValidationException("Insufficient funds for debit transaction");
        }
        account.setBalance(account.getBalance() - updated.getAmount());
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
        throw new ValidationException("Cannot revert transaction - insufficient funds");
      }
      account.setBalance(account.getBalance() + transaction.getAmount());
    } else {
      account.setBalance(account.getBalance() - transaction.getAmount());
    }

    accountRepository.save(account);
  }
}