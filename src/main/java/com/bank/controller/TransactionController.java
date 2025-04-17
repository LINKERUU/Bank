package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Transaction;
import com.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for managing bank transactions.
 * Provides CRUD operations for transactions including creation,
 * retrieval, updating and deletion.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Controller", description = "API для работы с транзакциями")
public class TransactionController {

  private final TransactionService transactionService;

  /**
   * Constructs a new TransactionController.
   *
   * @param transactionService service for transaction operations
   */
  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Retrieves all transactions.
   *
   * @return ResponseEntity containing list of all transactions
   */
  @Operation(summary = "Get all transactions",
          description = "Returns a list of all bank transactions")
  @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
  @GetMapping
  public ResponseEntity<List<Transaction>> getAllTransactions() {
    return ResponseEntity.ok(transactionService.findAllTransactions());
  }

  /**
   * Retrieves a transaction by its ID.
   *
   * @param id the ID of the transaction to retrieve
   * @return ResponseEntity containing the found transaction
   * @throws ResponseStatusException if transaction is not found or error occurs
   */
  @Operation(summary = "Get transaction by ID",
          description = "Returns a transaction by its identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Transaction found"),
      @ApiResponse(responseCode = "400", description = "Invalid ID format"),
      @ApiResponse(responseCode = "404", description = "Transaction not found")
  })
  @GetMapping("/{id}")
  public Transaction getTransactionById(@PathVariable Long id) {
    return transactionService.findTransactionById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction not found with ID: " + id
            ));
  }

  /**
   * Creates a new transaction.
   *
   * @param transaction the transaction data to create
   * @return ResponseEntity containing the created transaction
   */
  @Operation(summary = "Create new transaction",
          description = "Creates a new transaction. Supported types: "
                  + "'credit' (deposit), 'debit' (withdrawal)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Account not found"),
      @ApiResponse(responseCode = "409", description = "Insufficient funds")
  })
  @PostMapping
  public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody
                                                         Transaction transaction) {
    try {
      if (transaction.getTransactionType() != null) {
        transaction.setTransactionType(transaction.getTransactionType().toLowerCase());
      }
      Transaction created = transactionService.createTransaction(transaction);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (ValidationException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ResourceNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Updates an existing transaction.
   *
   * @param id the ID of the transaction to update
   * @param transaction the updated transaction data
   * @return ResponseEntity containing the updated transaction
   */
  @Operation(summary = "Update transaction",
          description = "Updates an existing transaction")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Transaction not found")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Transaction> updateTransaction(
          @PathVariable Long id,
          @Valid @RequestBody Transaction transaction) {
    try {
      Transaction updated = transactionService.updateTransaction(id, transaction);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Deletes a transaction by its ID.
   *
   * @param id the ID of the transaction to delete
   */
  @Operation(summary = "Delete transaction",
          description = "Deletes a transaction by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid ID format"),
      @ApiResponse(responseCode = "404", description = "Transaction not found")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(@PathVariable Long id) {
    try {
      transactionService.deleteTransaction(id);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}