package com.bank.service.impl;

import com.bank.model.Card;
import com.bank.model.Transaction;
import com.bank.repository.CardRepository;
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
  private final CardRepository cardRepository;

  /**
   * Constructs a new TransactionServiceImpl with the specified repositories.
   *
   * @param transactionRepository the repository for transaction operations
   * @param cardRepository the repository for card operations
   */
  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                CardRepository cardRepository) {
    this.transactionRepository = transactionRepository;
    this.cardRepository = cardRepository;
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
    // Find the card by cardId
    Card card = cardRepository.findById(transaction.getCardId())
            .orElseThrow(() -> new RuntimeException("Карта не найдена"));

    // Set the card in the transaction
    transaction.setCard(card);

    // Save the transaction
    return transactionRepository.save(transaction);
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Long id, Transaction transaction) {
    // Find the existing transaction by ID
    Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Транзакция с ID " + id + " не найдена"));

    // Update transaction fields
    if (transaction.getAmount() != null) {
      existingTransaction.setAmount(transaction.getAmount());
    }
    if (transaction.getTransactionType() != null) {
      existingTransaction.setTransactionType(transaction.getTransactionType());
    }
    if (transaction.getDescription() != null) {
      existingTransaction.setDescription(transaction.getDescription());
    }

    // Update the card if cardId is provided
    if (transaction.getCardId() != null) {
      Card newCard = cardRepository.findById(transaction.getCardId())
              .orElseThrow(() -> new RuntimeException("Карта с ID "
                      + transaction.getCardId() + " не найдена"));
      existingTransaction.setCard(newCard);
    } else {
      // If cardId is not provided, keep the current card
      existingTransaction.setCard(existingTransaction.getCard());
    }

    // Save the updated transaction
    return transactionRepository.save(existingTransaction);
  }

  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    transactionRepository.deleteById(id);
  }
}