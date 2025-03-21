package com.bank.service.impl;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.TransactionService;
import com.bank.utils.InMemoryCache;
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

  private static final String NOT_FOUND_MESSAGE = " not found";
  private static final String TRANSACTION_TYPE_DEBIT = "debit";
  private static final String TRANSACTION_TYPE_CREDIT = "credit";

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final InMemoryCache<String,
          List<Transaction>> transactionCache; // Cache for transactions list
  private final InMemoryCache<Long,
          Transaction> transactionByIdCache; // Cache for transactions by ID

  /**
   * Constructor for TransactionServiceImpl.
   *
   * @param transactionRepository the transaction repository
   * @param accountRepository the account repository
   * @param transactionCache cache for all transactions
   * @param transactionByIdCache cache for transactions by ID
   */
  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountRepository accountRepository,
                                InMemoryCache<String, List<Transaction>> transactionCache,
                                InMemoryCache<Long, Transaction> transactionByIdCache) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.transactionCache = transactionCache;
    this.transactionByIdCache = transactionByIdCache;
  }

  /**
   * Retrieves all transactions.
   *
   * @return list of transactions
   */
  @Override
  public List<Transaction> findAllTransactions() {
    String cacheKey = "all_transactions";
    List<Transaction> cachedTransactions = transactionCache.get(cacheKey);
    if (cachedTransactions != null) {
      return cachedTransactions;
    }

    List<Transaction> transactions = transactionRepository.findAll();
    transactionCache.put(cacheKey, transactions); // Store in cache
    return transactions;
  }

  /**
   * Finds a transaction by ID.
   *
   * @param id the transaction ID
   * @return an optional containing the transaction if found
   */
  @Override
  public Optional<Transaction> findTransactionById(Long id) {
    Transaction cachedTransaction = transactionByIdCache.get(id);
    if (cachedTransaction != null) {
      return Optional.of(cachedTransaction);
    }

    Optional<Transaction> transaction = transactionRepository.findById(id);
    transaction.ifPresent(t -> transactionByIdCache.put(id, t));
    return transaction;
  }

  /**
   * Creates a new transaction.
   *
   * @param transaction the transaction to be created
   * @return the created transaction
   */
  @Override
  @Transactional
  public Transaction createTransaction(Transaction transaction) {
    Account account = accountRepository.findById(transaction.getAccountId())
            .orElseThrow(() -> new RuntimeException("Account with ID "
                    + transaction.getAccountId() + NOT_FOUND_MESSAGE));

    transaction.setAccount(account);
    return transactionRepository.save(transaction);
  }

  /**
   * Updates an existing transaction.
   *
   * @param id the transaction ID
   * @param updatedTransaction the updated transaction data
   * @return the updated transaction
   */
  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
    Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction with ID "
                    + id + NOT_FOUND_MESSAGE));

    Account account = getAccount(updatedTransaction, existingTransaction);

    if (account.getBalance() < 0) {
      throw new IllegalArgumentException("Account balance cannot be negative!");
    }

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
              .orElseThrow(() -> new RuntimeException("Account with ID "
                      + updatedTransaction.getAccountId() + NOT_FOUND_MESSAGE));
      existingTransaction.setAccount(newAccount);
    } else {
      existingTransaction.setAccount(existingTransaction.getAccount());
    }

    return transactionRepository.save(existingTransaction);
  }

  /**
   * Retrieves the account and updates balances based on transaction type.
   *
   * @param updatedTransaction the updated transaction
   * @param existingTransaction the existing transaction
   * @return the updated account
   */
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
      } else if (TRANSACTION_TYPE_CREDIT
              .equalsIgnoreCase(updatedTransaction.getTransactionType())) {
        if (account.getBalance() - updatedTransaction.getAmount() < 0) {
          throw new IllegalArgumentException("Insufficient funds in the account!");
        }
        account.setBalance(account.getBalance() - updatedTransaction.getAmount());
      }
    }

    if (account.getBalance() < 0) {
      throw new IllegalArgumentException("Account balance cannot be negative!");
    }

    return account;
  }

  /**
   * Deletes a transaction.
   *
   * @param id the transaction ID
   */
  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction with ID "
                    + id + NOT_FOUND_MESSAGE));

    Account account = transaction.getAccount();

    if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transaction.getTransactionType())) {
      account.setBalance(account.getBalance() + transaction.getAmount());
    } else if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())) {
      if (account.getBalance() - transaction.getAmount() < 0) {
        throw new IllegalArgumentException("Deleting transaction will lead to a negative balance!");
      }
      account.setBalance(account.getBalance() - transaction.getAmount());
    }

    accountRepository.save(account);
    transactionRepository.deleteById(id);

    transactionByIdCache.evict(id);
    transactionCache.clear();
  }
}
