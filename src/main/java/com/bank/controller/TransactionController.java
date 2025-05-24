// TransactionController.java
package com.bank.controller;

import com.bank.dto.TransactionDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
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

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Controller", description = "API для работы с транзакциями")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @Operation(summary = "Get all transactions",
          description = "Returns a list of all bank transactions")
  @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
  @GetMapping
  public ResponseEntity<List<TransactionDto>> getAllTransactions() {
    return ResponseEntity.ok(transactionService.findAllTransactions());
  }

  @Operation(summary = "Get transaction by ID",
          description = "Returns a transaction by its identifier")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Transaction found"),
          @ApiResponse(responseCode = "400", description = "Invalid ID format"),
          @ApiResponse(responseCode = "404", description = "Transaction not found")
  })
  @GetMapping("/{id}")
  public TransactionDto getTransactionById(@PathVariable Long id) {
    return transactionService.findTransactionById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction not found with ID: " + id
            ));
  }

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
  public ResponseEntity<TransactionDto> createTransaction(@Valid @RequestBody
                                                          TransactionDto transactionDTO) {
    try {
      if (transactionDTO.getTransactionType() != null) {
        transactionDTO.setTransactionType(transactionDTO.getTransactionType().toLowerCase());
      }
      TransactionDto created = transactionService.createTransaction(transactionDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (ValidationException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ResourceNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Operation(summary = "Update transaction",
          description = "Updates an existing transaction")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "404", description = "Transaction not found")
  })
  @PutMapping("/{id}")
  public ResponseEntity<TransactionDto> updateTransaction(
          @PathVariable Long id,
          @Valid @RequestBody TransactionDto transactionDTO) {
    try {
      TransactionDto updated = transactionService.updateTransaction(id, transactionDTO);
      return ResponseEntity.ok(updated);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

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