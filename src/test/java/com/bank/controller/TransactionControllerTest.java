package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Transaction;
import com.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

  @Mock
  private TransactionService transactionService;

  @InjectMocks
  private TransactionController transactionController;

  private Transaction transaction1;
  private Transaction transaction2;

  @BeforeEach
  void setUp() {
    transaction1 = new Transaction();
    transaction1.setId(1L);
    transaction1.setAmount(100.0);
    transaction1.setTransactionType("credit");
    transaction1.setAccountId(1L);

    transaction2 = new Transaction();
    transaction2.setId(2L);
    transaction2.setAmount(50.0);
    transaction2.setTransactionType("debit");
    transaction2.setAccountId(2L);
  }

  @Test
  void getAllTransactions_ShouldReturnAllTransactions() {
    // Arrange
    List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
    when(transactionService.findAllTransactions()).thenReturn(transactions);

    // Act
    var response = transactionController.getAllTransactions();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
    verify(transactionService, times(1)).findAllTransactions();
  }

  @Test
  void getTransactionById_WithValidId_ShouldReturnTransaction() {
    // Arrange
    when(transactionService.findTransactionById(1L)).thenReturn(Optional.of(transaction1));

    // Act
    Transaction response = transactionController.getTransactionById(1L);

    // Assert
    assertEquals(1L, response.getId()); // Проверяем только тело
    verify(transactionService, times(1)).findTransactionById(1L);
  }

  @Test
  void getTransactionById_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(transactionService.findTransactionById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> transactionController.getTransactionById(99L));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Transaction not found with ID: 99"));
  }

  @Test
  void createTransaction_WithValidData_ShouldReturnCreatedTransaction() {
    // Arrange
    when(transactionService.createTransaction(any(Transaction.class))).thenReturn(transaction1);

    // Act
    var response = transactionController.createTransaction(transaction1);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
    verify(transactionService, times(1)).createTransaction(any(Transaction.class));
  }

  @Test
  void createTransaction_WithInvalidData_ShouldThrowException() {
    // Arrange
    transaction1.setAmount(-100.0);
    when(transactionService.createTransaction(any(Transaction.class)))
            .thenThrow(new ValidationException("Invalid amount"));

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> transactionController.createTransaction(transaction1));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Invalid amount"));
  }

  @Test
  void createTransaction_WithNonExistentAccount_ShouldThrowException() {
    // Arrange
    when(transactionService.createTransaction(any(Transaction.class)))
            .thenThrow(new ResourceNotFoundException("Account not found"));

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> transactionController.createTransaction(transaction1));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Account not found"));
  }

  @Test
  void updateTransaction_WithValidData_ShouldReturnUpdatedTransaction() {
    // Arrange
    when(transactionService.updateTransaction(eq(1L), any(Transaction.class))).thenReturn(transaction1);

    // Act
    var response = transactionController.updateTransaction(1L, transaction1);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
    verify(transactionService, times(1)).updateTransaction(eq(1L), any(Transaction.class));
  }

  @Test
  void updateTransaction_WithInvalidData_ShouldThrowException() {
    // Arrange
    when(transactionService.updateTransaction(eq(1L), any(Transaction.class)))
            .thenThrow(new ValidationException("Invalid transaction data"));

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> transactionController.updateTransaction(1L, transaction1));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Invalid transaction data"));
  }

  @Test
  void deleteTransaction_WithValidId_ShouldReturnNoContent() {
    // Arrange
    doNothing().when(transactionService).deleteTransaction(1L);

    // Act & Assert
    assertDoesNotThrow(() -> transactionController.deleteTransaction(1L));
    verify(transactionService, times(1)).deleteTransaction(1L);
  }

  @Test
  void deleteTransaction_WithInvalidId_ShouldThrowException() {
    // Arrange
    doThrow(new ValidationException("Invalid ID")).when(transactionService).deleteTransaction(0L);

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> transactionController.deleteTransaction(0L));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getMessage().contains("Invalid ID"));
  }
}