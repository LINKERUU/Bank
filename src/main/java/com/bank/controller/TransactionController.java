package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  private static final String TRANSACTION_TYPE_CREDIT = "credit";
  private static final String TRANSACTION_TYPE_DEBIT = "debit";
  private static final String TRANSACTION_NOT_FOUND_MESSAGE = "Not found transaction with ID ";

  private final TransactionService transactionService;
  private final AccountRepository accountRepository;

  /**
   * Constructs a new TransactionController with the specified services.
   *
   * @param transactionService the transaction service to be used
   * @param accountRepository  the account repository to be used
   */
  public TransactionController(TransactionService transactionService,
                               AccountRepository accountRepository) {
    this.transactionService = transactionService;
    this.accountRepository = accountRepository;
  }

  /**
   * Retrieves all transactions.
   *
   * @return a list of all transactions
   */
  @Operation(summary = "Получить все транзакции", description = "Возвращает список всех транзакций")
  @ApiResponse(responseCode = "200", description = "Транзакции успешно получены")
  @GetMapping
  public List<Transaction> findAllTransactions() {
    return transactionService.findAllTransactions();
  }

  /**
   * Retrieves a transaction by its ID.
   *
   * @param id the ID of the transaction
   * @return the transaction with the specified ID
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @Operation(summary = "Получить транзакцию по ID",
          description = "Возвращает транзакцию по ее идентификатору")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Транзакция найдена"),
      @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
  })
  @GetMapping("/{id}")
  public Transaction findTransactionById(@PathVariable Long id) {
    return transactionService.findTransactionById(id)
            .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MESSAGE + id));
  }

  /**
   * Creates a new transaction.
   *
   * @param transaction the transaction to create
   * @return the created transaction
   * @throws IllegalArgumentException if the account ID is null or the transaction type is invalid
   * @throws ResourceNotFoundException if the account is not found
   */
  @Operation(summary = "Создать новую транзакцию",
          description = "Создает новую транзакцию (кредит или дебет)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Транзакция успешно создана"),
      @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
      @ApiResponse(responseCode = "404", description = "Счет не найден"),
      @ApiResponse(responseCode = "409", description = "Недостаточно средств")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Transaction createTransaction(@RequestBody Transaction transaction) {
    if (transaction.getAccountId() == null) {
      throw new IllegalArgumentException("Account ID cannot be null");
    }

    String transactionType = transaction.getTransactionType().toLowerCase();

    if (!TRANSACTION_TYPE_CREDIT.equals(transactionType)
            && !TRANSACTION_TYPE_DEBIT.equals(transactionType)) {
      throw new IllegalArgumentException("Unknown transaction type: "
              + transaction.getTransactionType());
    }

    Account account = accountRepository.findById(transaction.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException("Account with ID "
                    + transaction.getAccountId() + " not found"));

    updateBalances(account, transaction);

    accountRepository.save(account);

    transaction.setAccount(account);
    return transactionService.createTransaction(transaction);
  }

  /**
   * Updates an existing transaction.
   *
   * @param id the ID of the transaction to update
   * @param transaction the updated transaction details
   * @return the updated transaction
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @Operation(summary = "Обновить транзакцию", description = "Обновляет существующую транзакцию")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Транзакция успешно обновлена"),
      @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
  })
  @PutMapping("/{id}")
  public Transaction updateTransaction(@PathVariable Long id,
                                       @RequestBody Transaction transaction) {
    if (transactionService.findTransactionById(id).isEmpty()) {
      throw new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MESSAGE + id);
    }
    return transactionService.updateTransaction(id, transaction);
  }

  /**
   * Deletes a transaction by its ID.
   *
   * @param id the ID of the transaction to delete
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @Operation(summary = "Удалить транзакцию", description = "Удаляет транзакцию по ее ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Транзакция успешно удалена"),
      @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTransaction(@PathVariable Long id) {
    if (transactionService.findTransactionById(id).isEmpty()) {
      throw new ResourceNotFoundException(TRANSACTION_NOT_FOUND_MESSAGE + id);
    }
    transactionService.deleteTransaction(id);
  }

  /**
   * Updates the account balance based on the transaction type.
   *
   * @param account the account to update
   * @param transaction the transaction to apply
   * @throws IllegalArgumentException if the transaction type is invalid or insufficient funds
   */
  private void updateBalances(Account account, Transaction transaction) {
    double amount = transaction.getAmount();
    String transactionType = transaction.getTransactionType().toLowerCase();

    if (!TRANSACTION_TYPE_CREDIT.equals(transactionType)
            && !TRANSACTION_TYPE_DEBIT.equals(transactionType)) {
      throw new IllegalArgumentException("Unknown transaction type: "
              + transaction.getTransactionType());
    }

    if (TRANSACTION_TYPE_DEBIT.equals(transactionType)) {
      account.setBalance(account.getBalance() + amount);
    } else {
      if (account.getBalance() - amount < 0) {
        throw new IllegalArgumentException("Insufficient funds in the account!");
      }
      account.setBalance(account.getBalance() - amount);
    }

    // Ensure balance does not become negative
    if (account.getBalance() < 0) {
      throw new IllegalArgumentException("Account balance cannot be negative!");
    }
  }
}
