package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Card;
import com.bank.model.Transaction;
import com.bank.repository.CardRepository;
import com.bank.service.TransactionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller for managing bank transactions.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;
  private final CardRepository cardRepository;

  /**
   * Constructs a new TransactionController with the specified services.
   *
   * @param transactionService the transaction service to be used
   * @param cardRepository     the card repository to be used
   */
  public TransactionController(TransactionService transactionService,
      CardRepository cardRepository) {
    this.transactionService = transactionService;
    this.cardRepository = cardRepository;
  }

  /**
   * Retrieves all transactions.
   *
   * @return a list of all transactions
   */
  @GetMapping
  public List<Transaction> findAllTransactions() {
    return transactionService.findAllTransactions();
  }

  /**
   * Retrieves a transaction by its ID.
   *
   * @param id the ID of the transaction to retrieve
   * @return the transaction with the specified ID
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @GetMapping("/{id}")
  public Transaction findTransactionById(@PathVariable Long id) {
    return transactionService.findTransactionById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Not found transaction with ID" + id));
  }

  /**
   * Creates a new transaction.
   *
   * @param transaction the transaction to create
   * @return the created transaction
   * @throws IllegalArgumentException if the card ID is null or the transaction type is invalid
   * @throws ResourceNotFoundException if the card is not found
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Transaction createTransaction(@RequestBody Transaction transaction) {
    if (transaction.getCardId() == null) {
      throw new IllegalArgumentException("Card ID cannot be null");
    }

    String transactionType = transaction.getTransactionType().toLowerCase();

    if (!"credit".equals(transactionType) && !"debit".equals(transactionType)) {
      throw new IllegalArgumentException("Неизвестный тип транзакции: "
              + transaction.getTransactionType());
    }

    Card card = cardRepository.findById(transaction.getCardId())
            .orElseThrow(() -> new ResourceNotFoundException("Карта с ID "
                    + transaction.getCardId() + " не найдена"));

    transaction.setCard(card);
    updateCardBalance(card, transaction);
    cardRepository.save(card);
    return transactionService.createTransaction(transaction);
  }

  /**
   * Updates an existing transaction.
   *
   * @param id          the ID of the transaction to update
   * @param transaction the updated transaction details
   * @return the updated transaction
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @PutMapping("/{id}")
  public Transaction updateTransaction(@PathVariable Long id,
                                       @RequestBody Transaction transaction) {
    if (transactionService.findTransactionById(id).isEmpty()) {
      throw new ResourceNotFoundException("Not found transaction with ID" + id);
    }
    return transactionService.updateTransaction(id, transaction);
  }

  /**
   * Deletes a transaction by its ID.
   *
   * @param id the ID of the transaction to delete
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(@PathVariable Long id) {
    if (transactionService.findTransactionById(id).isEmpty()) {
      throw new ResourceNotFoundException("Not found transaction with ID " + id);
    }
    transactionService.deleteTransaction(id);
  }

  /**
   * Updates the balance of a card based on the transaction.
   *
   * @param card        the card to update
   * @param transaction the transaction to apply
   * @throws IllegalArgumentException if the transaction type is invalid
   */
  private void updateCardBalance(Card card, Transaction transaction) {
    double amount = transaction.getAmount();
    String transactionType = transaction.getTransactionType().toLowerCase();

    if (!"credit".equals(transactionType) && !"debit".equals(transactionType)) {
      throw new IllegalArgumentException("Неизвестный тип транзакции: "
              + transaction.getTransactionType());
    }

    if ("credit".equals(transactionType)) {
      card.setBalance(card.getBalance() + amount);
    } else {
      card.setBalance(card.getBalance() - amount);
    }

    cardRepository.save(card);
  }
}