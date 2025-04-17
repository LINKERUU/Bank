package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

  @Mock private AccountService accountService;
  @InjectMocks private AccountController accountController;

  private Account createTestAccount(Long id, String number, double balance) {
    Account account = new Account();
    account.setId(id);
    account.setAccountNumber(number);
    account.setBalance(balance);
    return account;
  }

  @Test
  void findAllAccounts_ShouldReturnAllAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountService.findAllAccounts()).thenReturn(List.of(account));

    List<Account> result = accountController.findAllAccounts();

    assertEquals(1, result.size());
    assertEquals("1234567890", result.get(0).getAccountNumber()); // Исправлено здесь
  }

  @Test
  void findAccountById_ShouldReturnAccount() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountService.findAccountById(1L)).thenReturn(Optional.of(account));

    Optional<Account> result = accountController.findAccountById(1L);

    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
  }

  @Test
  void createAccount_ShouldReturnCreatedResponse() {
    Account account = createTestAccount(null, "1234567890", 1000.0);
    Account savedAccount = createTestAccount(1L, "1234567890", 1000.0);

    when(accountService.createAccount(account)).thenReturn(savedAccount);

    ResponseEntity<Account> response = accountController.createAccount(account);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
  }

  @Test
  void createAccounts_ShouldReturnCreatedAccounts() {
    Account account = createTestAccount(null, "1234567890", 1000.0);
    Account savedAccount = createTestAccount(1L, "1234567890", 1000.0);

    when(accountService.createAccounts(List.of(account))).thenReturn(List.of(savedAccount));

    List<Account> result = accountController.createAccounts(List.of(account));

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId()); // Исправлено здесь
  }

  @Test
  void updateAccount_ShouldReturnUpdatedAccount() {
    Account updates = createTestAccount(1L, null, 2000.0);
    Account updated = createTestAccount(1L, "1234567890", 2000.0);

    when(accountService.updateAccount(1L, updates)).thenReturn(updated);

    Account result = accountController.updateAccount(1L, updates);

    assertEquals(2000.0, result.getBalance());
  }

  @Test
  void updateAccounts_ShouldReturnUpdatedAccounts() {
    Account account = createTestAccount(1L, "1234567890", 2000.0);
    when(accountService.updateAccounts(List.of(account))).thenReturn(List.of(account));

    ResponseEntity<List<Account>> response =
            accountController.updateAccounts(List.of(account));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void deleteAccount_ShouldCallService() {
    doNothing().when(accountService).deleteAccount(1L);

    accountController.deleteAccount(1L);

    verify(accountService).deleteAccount(1L);
  }

  @Test
  void deleteAccounts_ShouldCallService() {
    doNothing().when(accountService).deleteAccounts(List.of(1L, 2L));

    accountController.deleteAccounts(List.of(1L, 2L));

    verify(accountService).deleteAccounts(List.of(1L, 2L));
  }

  @Test
  void getAccountsByUserEmail_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountService.findByUserEmail("test@example.com")).thenReturn(List.of(account));

    ResponseEntity<List<Account>> response =
            accountController.getAccountsByUserEmail("test@example.com");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void getAccountsWithCards_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountService.findAccountsWithCards()).thenReturn(List.of(account));

    ResponseEntity<List<Account>> response =
            accountController.getAccountsWithCards();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }
}