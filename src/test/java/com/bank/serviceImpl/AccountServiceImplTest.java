package com.bank.serviceImpl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.serviceImpl.impl.AccountServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock private AccountRepository accountRepository;
  @Mock private CardRepository cardRepository;
  @Mock private InMemoryCache<Long, Account> accountCache;
  @InjectMocks private AccountServiceImpl accountService;

  private Account createTestAccount(Long id, String number, double balance) {
    Account account = new Account();
    account.setId(id);
    account.setAccountNumber(number);
    account.setBalance(balance);
    account.setUsers((java.util.Set<User>) Collections.singletonList(new User()));
    return account;
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
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    Optional<Account> result = accountService.findAccountById(1L);

    assertTrue(result.isPresent());
    verify(accountRepository).findById(1L);
    verify(accountCache).put(1L, account);
  }

  @Test
  void createAccount_ShouldValidateAndSaveAccount() {
    Account account = createTestAccount(null, "1234567890", 1000.0);
    Account savedAccount = createTestAccount(1L, "1234567890", 1000.0);

    when(accountRepository.save(account)).thenReturn(savedAccount);

    Account result = accountService.createAccount(account);

    assertNotNull(result.getId());
    verify(accountRepository).save(account);
    verify(accountCache).put(1L, savedAccount);
  }

  @Test
  void createAccount_ShouldThrowValidationExceptionForInvalidData() {
    Account invalidAccount = new Account();
    invalidAccount.setAccountNumber("short");
    invalidAccount.setBalance(-100.0);

    assertThrows(ValidationException.class, () -> accountService.createAccount(invalidAccount));
    verifyNoInteractions(accountRepository);
  }

  @Test
  void createAccounts_ShouldValidateAndSaveAllAccounts() {
    Account account1 = createTestAccount(null, "1111111111", 1000.0);
    Account account2 = createTestAccount(null, "2222222222", 2000.0);
    List<Account> accounts = List.of(account1, account2);

    when(accountRepository.saveAll(accounts)).thenReturn(accounts);

    List<Account> result = accountService.createAccounts(accounts);

    assertEquals(2, result.size());
    verify(accountRepository).saveAll(accounts);
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
    assertEquals("1234567890", result.getAccountNumber()); // Не должно измениться
    verify(accountCache).put(1L, existing);
  }

  @Test
  void updateAccount_ShouldThrowWhenAccountNotFound() {
    when(accountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> accountService.updateAccount(1L, new Account()));
  }

  @Test
  void updateAccounts_ShouldUpdateMultipleAccounts() {
    Account account1 = createTestAccount(1L, "1111111111", 1000.0);
    Account account2 = createTestAccount(2L, "2222222222", 2000.0);
    List<Account> accounts = List.of(account1, account2);

    when(accountRepository.findById(1L)).thenReturn(Optional.of(account1));
    when(accountRepository.findById(2L)).thenReturn(Optional.of(account2));
    when(accountRepository.saveAll(anyList())).thenReturn(accounts);

    List<Account> result = accountService.updateAccounts(accounts);

    assertEquals(2, result.size());
    verify(accountCache, times(2)).put(anyLong(), any());
  }

  @Test
  void deleteAccount_ShouldDeleteWithCards() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

    accountService.deleteAccount(1L);

    verify(cardRepository).deleteAll(any());
    verify(accountRepository).delete(account);
    verify(accountCache).evict(1L);
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
  void findByUserEmail_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountRepository.findByUserEmail("test@example.com")).thenReturn(List.of(account));

    List<Account> result = accountService.findByUserEmail("test@example.com");

    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst().getId());
  }

  @Test
  void findAccountsWithCards_ShouldReturnAccounts() {
    Account account = createTestAccount(1L, "1234567890", 1000.0);
    when(accountRepository.findAccountsWithCards()).thenReturn(List.of(account));

    List<Account> result = accountService.findAccountsWithCards();

    assertEquals(1, result.size());
    assertEquals(1L, result.getFirst().getId());
  }
}