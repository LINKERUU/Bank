package com.bank.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private AccountRepository accountRepository;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  private Transaction transaction;
  private Account account;

  @BeforeEach
  void setUp() {
    account = new Account();
    account.setId(1L);
    account.setBalance(1000.0);

    transaction = new Transaction();
    transaction.setId(1L);
    transaction.setAmount(100.0);
    transaction.setTransactionType("credit");
    transaction.setAccountId(1L);
  }

  @Test
  void findAllTransactions_ShouldReturnAllTransactions() {
    // Arrange
    Transaction transaction2 = new Transaction();
    transaction2.setId(2L);
    List<Transaction> expectedTransactions = Arrays.asList(transaction, transaction2);
    when(transactionRepository.findAll()).thenReturn(expectedTransactions);

    // Act
    List<Transaction> result = transactionService.findAllTransactions();

    // Assert
    assertEquals(2, result.size());
    verify(transactionRepository, times(1)).findAll();
  }

  @Test
  void findTransactionById_WithValidId_ShouldReturnTransaction() {
    // Arrange
    when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

    // Act
    Optional<Transaction> result = transactionService.findTransactionById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verify(transactionRepository, times(1)).findById(1L);
  }

  @Test
  void findTransactionById_WithInvalidId_ShouldThrowException() {
    // Act & Assert
    assertThrows(ValidationException.class, () -> transactionService.findTransactionById(0L));
    assertThrows(ValidationException.class, () -> transactionService.findTransactionById(null));
  }

  @Test
  void createTransaction_WithValidCredit_ShouldCreateTransaction() {
    // Arrange
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

    // Act
    Transaction result = transactionService.createTransaction(transaction);

    // Assert
    assertNotNull(result);
    assertEquals(900.0, account.getBalance()); // 1000 - 100
    verify(accountRepository, times(1)).save(account);
    verify(transactionRepository, times(1)).save(any(Transaction.class));
  }

  @Test
  void createTransaction_WithValidDebit_ShouldCreateTransaction() {
    // Arrange
    transaction.setTransactionType("debit");
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

    // Act
    Transaction result = transactionService.createTransaction(transaction);

    // Assert
    assertNotNull(result);
    assertEquals(1100.0, account.getBalance()); // 1000 + 100
    verify(accountRepository, times(1)).save(account);
    verify(transactionRepository, times(1)).save(any(Transaction.class));
  }

  @Test
  void createTransaction_WithInsufficientFunds_ShouldThrowException() {
    // Arrange
    transaction.setAmount(2000.0);
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> transactionService.createTransaction(transaction));
    assertEquals("Insufficient funds for debit transaction", exception.getMessage());
    assertEquals(1000.0, account.getBalance()); // Balance should not change
  }

  @Test
  void createTransaction_WithInvalidTransactionType_ShouldThrowException() {
    // Arrange
    transaction.setTransactionType("invalid");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> transactionService.createTransaction(transaction));
    assertEquals("Transaction type must be 'credit' or 'debit'", exception.getMessage());
  }

  @Test
  void createTransaction_WithNullAmount_ShouldThrowException() {
    // Arrange
    transaction.setAmount(null);

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> transactionService.createTransaction(transaction));
    assertEquals("Transaction amount must be positive", exception.getMessage());
  }

  @Test
  void createTransaction_WithNonExistentAccount_ShouldThrowException() {
    // Arrange
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> transactionService.createTransaction(transaction));
    assertEquals("Account not found with ID: 1", exception.getMessage());
  }

  @Test
  void updateTransaction_WithValidData_ShouldUpdateTransaction() {
    // Arrange
    Account account = new Account();
    account.setId(1L);
    account.setBalance(1000.0); // Явно устанавливаем начальный баланс

    Transaction existingTransaction = new Transaction();
    existingTransaction.setId(1L);
    existingTransaction.setAmount(50.0);
    existingTransaction.setTransactionType("credit");
    existingTransaction.setAccount(account);

    Transaction updatedTransaction = new Transaction();
    updatedTransaction.setAmount(100.0);
    updatedTransaction.setTransactionType("debit");
    updatedTransaction.setAccountId(1L);

    when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction saved = invocation.getArgument(0);
      account.setBalance(account.getBalance() - existingTransaction.getAmount()); // Отменяем старую транзакцию
      account.setBalance(account.getBalance() + (saved.getTransactionType().equals("credit") ? saved.getAmount() : -saved.getAmount()));
      return saved;
    });

    // Act
    Transaction result = transactionService.updateTransaction(1L, updatedTransaction);

    // Assert
    assertNotNull(result);
    assertEquals(950.0, account.getBalance()); // 1000 - 50 (отмена credit) - 100 (новый debit)
    assertEquals(100.0, result.getAmount());
    assertEquals("debit", result.getTransactionType());
    verify(transactionRepository, times(1)).save(existingTransaction);
  }

  @Test
  void updateTransaction_WithInvalidId_ShouldThrowException() {
    // Act & Assert
    assertThrows(ValidationException.class,
            () -> transactionService.updateTransaction(0L, transaction));
  }

  @Test
  void deleteTransaction_WithValidId_ShouldDeleteTransaction() {
    // Arrange
    transaction.setAccount(account);
    when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

    // Act
    transactionService.deleteTransaction(1L);

    // Assert
    assertEquals(1100.0, account.getBalance()); // 1000 + 100 (credit reversal)
    verify(transactionRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteTransaction_WithDebitTransaction_ShouldRestoreBalance() {
    // Arrange
    transaction.setTransactionType("debit");
    transaction.setAccount(account);
    when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

    // Act
    transactionService.deleteTransaction(1L);

    // Assert
    assertEquals(900.0, account.getBalance()); // 1000 - 100 (debit reversal)
    verify(transactionRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteTransaction_WithInsufficientFunds_ShouldThrowException() {
    // Arrange
    transaction.setTransactionType("credit");
    transaction.setAmount(2000.0);
    account.setBalance(100.0);
    transaction.setAccount(account);
    when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> transactionService.deleteTransaction(1L));
    assertEquals("Cannot revert transaction - insufficient funds", exception.getMessage());
    assertEquals(100.0, account.getBalance()); // Balance should not change
  }
}