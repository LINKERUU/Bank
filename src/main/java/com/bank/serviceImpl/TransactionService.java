package com.bank.serviceImpl;

import com.bank.model.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing transactions.
 */
public interface TransactionService {

  /**
   * Retrieves all transactions.
   *
   * @return a list of all transactions
   */
  List<Transaction> findAllTransactions();

  /**
   * Retrieves a transaction by its ID.
   *
   * @param id the ID of the transaction to retrieve
   * @return an Optional containing the transaction if found, otherwise empty
   */
  Optional<Transaction> findTransactionById(Long id);

  /**
   * Creates a new transaction.
   *
   * @param transaction the transaction to create
   * @return the created transaction
   */
  Transaction createTransaction(Transaction transaction);

  /**
   * Updates an existing transaction.
   *
   * @param id the ID of the transaction to update
   * @param transaction the transaction details to update
   * @return the updated transaction
   */
  Transaction updateTransaction(Long id, Transaction transaction);

  /**
   * Deletes a transaction by its ID.
   *
   * @param id the ID of the transaction to delete
   */
  void deleteTransaction(Long id);
}