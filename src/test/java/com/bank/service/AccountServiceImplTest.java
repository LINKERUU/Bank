package com.bank.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Card;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.service.impl.AccountServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock private AccountRepository accountRepository;
  @Mock private CardRepository cardRepository;
  @Mock private InMemoryCache<Long, Account> accountCache;
  @InjectMocks private AccountServiceImpl accountService;

  private Account createTestAccount(Long id, String accountNumber, Double balance) {
    Account account = new Account();
    account.setId(id);
    account.setAccountNumber(accountNumber);
    account.setBalance(balance);
    return account;
  }

  @Test
  void findAllAccounts_ShouldReturnAllAccounts() {
    Account account1 = createTestAccount(1L, "1234567890", 1000.0);
    Account account2 = createTestAccount(2L, "0987654321", 2000.0);
    when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

    List<Account> result = accountService.findAllAccounts();

    assertEquals(2, result.size());
    verify(accountRepository).findAll();
    verifyNoInteractions(accountCache);
  }

  @Test
  void findAllAccounts_ShouldReturnEmptyList() {
    when(accountRepository.findAll()).thenReturn(Collections.emptyList());

    List<Account> result = accountService.findAllAccounts();

    assertTrue(result.isEmpty());
  }

  @Test
  void findAccountById_ShouldReturnCachedAccount() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountCache.get(1L)).thenReturn(account);

    Optional<Account> result = accountService.findAccountById(1L);

    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verify(accountCache).get(1L);
    verifyNoInteractions(accountRepository);
  }

  @Test
  void findAccountById_ShouldFetchFromRepositoryWhenNotCached() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountCache.get(1L)).thenReturn(null);
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    Optional<Account> result = accountService.findAccountById(1L);

    assertTrue(result.isPresent());
    verify(accountRepository).findById(1L);
    verify(accountCache).put(1L, account);
  }

  @Test
  void findAccountById_ShouldReturnEmptyWhenNotFound() {
    when(accountCache.get(1L)).thenReturn(null);
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    Optional<Account> result = accountService.findAccountById(1L);

    assertFalse(result.isPresent());
    verify(accountRepository).findById(1L);
    verify(accountCache, never()).put(anyLong(), any());
  }

  @Test
  void createAccount_ShouldValidateAndSaveAccount() {
    User user = new User();
    user.setId(1L);

    Account account = createTestAccount(null, "1234567890", 1000.0);
    account.setUsers(Set.of(user));

    Account savedAccount = createTestAccount(1L, "1234567890", 1000.0);
    savedAccount.setUsers(Set.of(user));

    when(accountRepository.save(account)).thenReturn(savedAccount);

    Account result = accountService.createAccount(account);

    assertNotNull(result.getId());
    assertEquals(1, result.getUsers().size());
    verify(accountRepository).save(account);
    verify(accountCache).put(1L, savedAccount);
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForInvalidAccountNumber() {
    Account account = createTestAccount(null, "short", 1000.0);
    assertThrows(ValidationException.class, () -> accountService.createAccount(account));
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForNonNumericAccountNumber() {
    Account account = createTestAccount(null, "123abc4567", 1000.0);
    assertThrows(ValidationException.class, () -> accountService.createAccount(account));
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForNegativeBalance() {
    Account account = createTestAccount(null, "1234567890", -100.0);
    assertThrows(ValidationException.class, () -> accountService.createAccount(account));
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForNullBalance() {
    Account account = createTestAccount(null, "1234567890", (double) 0);
    account.setBalance(null);
    assertThrows(ValidationException.class, () -> accountService.createAccount(account));
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForEmptyUsers() {
    Account account = createTestAccount(null, "1234567890", 1000.0);
    account.setUsers(Collections.emptySet());

    assertThrows(ValidationException.class, () -> accountService.createAccount(account));
    verifyNoInteractions(accountRepository);
  }

  @Test
  void createAccounts_ShouldValidateAndSaveAllAccounts() {
    User user = new User();
    user.setId(1L);

    Account account1 = createTestAccount(1L, "1111111111", 1000.0);
    account1.setUsers(Set.of(user));
    Account account2 = createTestAccount(2L, "2222222222", 2000.0);
    account2.setUsers(Set.of(user));
    List<Account> accounts = List.of(account1, account2);

    when(accountRepository.saveAll(anyList())).thenReturn(accounts);

    List<Account> result = accountService.createAccounts(accounts);

    assertEquals(2, result.size());
    verify(accountRepository).saveAll(anyList());
    verify(accountCache, times(2)).put(anyLong(), any());
  }

  @Test
  void createAccounts_ShouldThrowValidationExceptionForInvalidBatch() {
    Account valid = createTestAccount(null, "1111111111", 1000.0);
    Account invalid = createTestAccount(null, "short", -100.0);
    List<Account> accounts = List.of(valid, invalid);

    assertThrows(ValidationException.class, () -> accountService.createAccounts(accounts));
    verifyNoInteractions(accountRepository);
  }

  @Test
  void updateAccount_ShouldUpdateExistingAccount() {
    Account existing = createTestAccount(1L, "1234567890", 1000.0);
    Account updates = createTestAccount(1L, null, 2000.0);

    when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(accountRepository.save(existing)).thenReturn(existing);

    Account result = accountService.updateAccount(1L, updates);

    assertEquals(2000.0, result.getBalance());
    assertEquals("1234567890", result.getAccountNumber());
    verify(accountCache).put(1L, existing);
  }

  @Test
  void updateAccount_ShouldThrowWhenAccountNotFound() {
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> accountService.updateAccount(1L, new Account()));
  }

  @Test
  void updateAccount_ShouldUpdateAccountNumberWhenProvided() {
    Account existing = createTestAccount(1L, "1234567890", 1000.0);
    Account updates = createTestAccount(1L, "9876543210", null);

    when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(accountRepository.save(existing)).thenReturn(existing);

    Account result = accountService.updateAccount(1L, updates);

    assertEquals("9876543210", result.getAccountNumber());
    assertEquals(1000.0, result.getBalance());
  }


  @Test
  void updateAccounts_ShouldThrowWhenAccountIsNull() {
    List<Account> accounts = new ArrayList<>();
    accounts.add(createTestAccount(1L, "111", 1000.0));
    accounts.add(null);

    assertThrows(ValidationException.class,
            () -> accountService.updateAccounts(accounts));
  }

  @Test
  void updateAccounts_ShouldThrowForNullList() {
    assertThrows(ValidationException.class, () -> accountService.updateAccounts(null));
  }

  @Test
  void deleteAccount_ShouldDeleteWithCards() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    account.setCards(Set.of(new Card()));

    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    accountService.deleteAccount(1L);

    verify(cardRepository).deleteAll(account.getCards());
    verify(accountRepository).delete(account);
    verify(accountCache).evict(1L);
  }

  @Test
  void deleteAccount_ShouldDeleteWithoutCards() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    account.setCards(Collections.emptySet());

    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    accountService.deleteAccount(1L);

    verify(cardRepository, never()).deleteAll(any());
    verify(accountRepository).delete(account);
    verify(accountCache).evict(1L);
  }

  @Test
  void deleteAccount_ShouldThrowWhenAccountNotFound() {
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> accountService.deleteAccount(1L));
  }

  @Test
  void deleteAccounts_ShouldDeleteMultipleAccounts() {
    Account account1 = createTestAccount(1L, "1111111111", 1000.0);
    Account account2 = createTestAccount(2L, "2222222222", 2000.0);

    when(accountRepository.findById(1L)).thenReturn(Optional.of(account1));
    when(accountRepository.findById(2L)).thenReturn(Optional.of(account2));

    accountService.deleteAccounts(List.of(1L, 2L));

    verify(accountRepository, times(2)).delete(any());
    verify(accountCache, times(2)).evict(anyLong());
  }

  @Test
  void deleteAccounts_ShouldThrowWhenAccountNotFound() {
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> accountService.deleteAccounts(List.of(1L)));
  }

  @Test
  void findByUserEmail_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountRepository.findByUserEmail("test@example.com")).thenReturn(List.of(account));

    List<Account> result = accountService.findByUserEmail("test@example.com");

    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst().getId());
  }

  @Test
  void findByUserEmail_ShouldReturnEmptyListWhenNoAccounts() {
    when(accountRepository.findByUserEmail("test@example.com")).thenReturn(Collections.emptyList());

    List<Account> result = accountService.findByUserEmail("test@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  void findAccountsWithCards_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountRepository.findAccountsWithCards()).thenReturn(List.of(account));

    List<Account> result = accountService.findAccountsWithCards();

    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst().getId());
  }

  @Test
  void findAccountsWithCards_ShouldReturnEmptyListWhenNoAccounts() {
    when(accountRepository.findAccountsWithCards()).thenReturn(Collections.emptyList());

    List<Account> result = accountService.findAccountsWithCards();

    assertTrue(result.isEmpty());
  }

  @Test
  void validateAccount_ShouldThrowForInvalidAccountNumber() {
    Account account = createTestAccount(null, "short", 1000.0);
    assertThrows(ValidationException.class, () -> accountService.validateAccount(account));
  }

  @Test
  void validateAccount_ShouldThrowForNegativeBalance() {
    Account account = createTestAccount(null, "1234567890", -100.0);
    assertThrows(ValidationException.class, () -> accountService.validateAccount(account));
  }

  @Test
  void validateAccount_ShouldThrowForNullAccountNumber() {
    Account account = createTestAccount(null, null, 1000.0);
    assertThrows(ValidationException.class, () -> accountService.validateAccount(account));
  }

  @Test
  void validateAccount_ShouldPassForValidAccount() {
    Account account = createTestAccount(null, "1234567890", 1000.0);
    assertDoesNotThrow(() -> accountService.validateAccount(account));
  }

  @Test
  void findAccountById_ShouldReturnEmptyOptionalForNullId() {
    Optional<Account> result = accountService.findAccountById(null);
    assertFalse(result.isPresent());
  }

  @Test
  void createAccounts_ShouldReturnEmptyListForEmptyInput() {
    List<Account> result = accountService.createAccounts(Collections.emptyList());
    assertTrue(result.isEmpty());
  }


  @Test
  void deleteAccounts_ShouldDoNothingForEmptyList() {
    accountService.deleteAccounts(Collections.emptyList());
    verifyNoInteractions(accountRepository, accountCache, cardRepository);
  }


  @Test
  void validateAccount_ShouldThrowForZeroBalance() {
    Account account = createTestAccount(null, "1234567890", 0.0);
    assertDoesNotThrow(() -> accountService.validateAccount(account));
  }

  @Test
  void createAccount_ShouldHandleMultipleUsers() {
    User user1 = new User(); user1.setId(1L);
    User user2 = new User(); user2.setId(2L);

    Account account = createTestAccount(null, "1234567890", 1000.0);
    account.setUsers(Set.of(user1, user2));

    Account savedAccount = createTestAccount(1L, "1234567890", 1000.0);
    savedAccount.setUsers(Set.of(user1, user2));

    when(accountRepository.save(account)).thenReturn(savedAccount);

    Account result = accountService.createAccount(account);

    assertEquals(2, result.getUsers().size());
    verify(accountCache).put(1L, savedAccount);
  }
}