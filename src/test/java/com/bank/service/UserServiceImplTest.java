package com.bank.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.impl.UserServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordService passwordService;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private InMemoryCache<Long, User> userCache;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private User testUser2;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@example.com");
    testUser.setPhone("+1234567890");
    testUser.setPasswordHash("ValidPass1!");

    testUser2 = new User();
    testUser2.setId(2L);
    testUser2.setFirstName("Jane");
    testUser2.setLastName("Smith");
    testUser2.setEmail("jane.smith@example.com");
    testUser2.setPhone("+9876543210");
    testUser2.setPasswordHash("AnotherValid1!");
  }

  @Test
  void findAllUsers_ShouldReturnAllUsers() {
    when(userRepository.findAll()).thenReturn(List.of(testUser, testUser2));

    List<User> users = userService.findAllUsers();

    assertEquals(2, users.size());
    verify(userRepository).findAll();
  }

  @Test
  void findUserById_ShouldReturnUserFromCache() {
    when(userCache.get(1L)).thenReturn(testUser);

    Optional<User> result = userService.findUserById(1L);

    assertTrue(result.isPresent());
    assertEquals(testUser, result.get());
    verify(userCache).get(1L);
    verify(userRepository, never()).findById(anyLong());
  }

  @Test
  void findUserById_ShouldReturnUserFromRepositoryAndCacheIt() {
    when(userCache.get(1L)).thenReturn(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    Optional<User> result = userService.findUserById(1L);

    assertTrue(result.isPresent());
    assertEquals(testUser, result.get());
    verify(userCache).put(1L, testUser);
  }

  @Test
  void findUserById_ShouldReturnEmptyForNonExistingUser() {
    when(userCache.get(1L)).thenReturn(null);
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Optional<User> result = userService.findUserById(1L);

    assertTrue(result.isEmpty());
    verify(userCache, never()).put(anyLong(), any());
  }

  @Test
  void createUser_ShouldCreateAndCacheUser() {
    when(passwordService.hashPassword("ValidPass1!")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User created = userService.createUser(testUser);

    assertNotNull(created);
    verify(passwordService).hashPassword("ValidPass1!");  // Match actual password
    verify(userRepository).save(testUser);
    verify(userCache).put(testUser.getId(), testUser);
  }

  @Test
  void createUser_ShouldThrowValidationExceptionForNullUser() {
    assertThrows(ValidationException.class, () -> userService.createUser(null));
  }

  @Test
  void createUser_ShouldThrowValidationExceptionForInvalidEmail() {
    testUser.setEmail(null);
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));

    testUser.setEmail("  ");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }

  @Test
  void createUser_ShouldThrowValidationExceptionForInvalidPhone() {
    testUser.setPhone(null);
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));

    testUser.setPhone("  ");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }

  @Test
  void createUser_ShouldThrowValidationExceptionForWeakPassword() {
    testUser.setPasswordHash("weak");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }

  @Test
  void createUsers_ShouldCreateMultipleUsers() {
    when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
    when(userRepository.saveAll(anyList())).thenReturn(List.of(testUser, testUser2));

    List<User> createdUsers = userService.createUsers(List.of(testUser, testUser2));

    assertEquals(2, createdUsers.size());
    verify(passwordService, times(2)).hashPassword(anyString());
    verify(userRepository).saveAll(anyList());
    verify(userCache, times(2)).put(anyLong(), any(User.class));
  }

  @Test
  void updateUser_ShouldUpdateExistingUser() {
    User updatedData = new User();
    updatedData.setFirstName("Updated");
    updatedData.setLastName("Name");
    updatedData.setEmail("updated@example.com");
    updatedData.setPhone("+9999999999");
    updatedData.setPasswordHash("NewPass1!");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(passwordService.hashPassword(anyString())).thenReturn("hashedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    User updated = userService.updateUser(1L, updatedData);

    assertEquals("Updated", updated.getFirstName());
    assertEquals("Name", updated.getLastName());
    assertEquals("updated@example.com", updated.getEmail());
    assertEquals("+9999999999", updated.getPhone());
    verify(userCache).put(1L, testUser);
  }

  @Test
  void updateUser_ShouldThrowWhenUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, testUser));
  }

  @Test
  void updateUser_ShouldValidateEmailUniqueness() {
    User updatedData = new User();
    updatedData.setEmail("existing@example.com");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(ValidationException.class, () -> userService.updateUser(1L, updatedData));
  }

  @Test
  void updateUser_ShouldValidatePhoneUniqueness() {
    User updatedData = new User();
    updatedData.setPhone("+9999999999");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByPhone("+9999999999")).thenReturn(true);  // Changed from existsById

    assertThrows(ValidationException.class, () -> userService.updateUser(1L, updatedData));
  }

  @Test
  void updateUser_ShouldValidatePasswordStrength() {
    User updatedData = new User();
    updatedData.setPasswordHash("weak");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    assertThrows(ValidationException.class, () -> userService.updateUser(1L, updatedData));
  }

  @Test
  void deleteUser_ShouldDeleteUserAndEvictCache() {
    Account account = new Account();
    account.setUsers(new HashSet<>(Collections.singleton(testUser))); // mutable set

    testUser.setAccounts(Collections.singleton(account));

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.deleteUser(1L);

    verify(accountRepository).delete(account);
    verify(userRepository).delete(testUser);
    verify(userCache).evict(1L);
  }

  @Test
  void deleteUser_ShouldNotDeleteAccountIfOtherUsersExist() {
    Account account = new Account();
    User otherUser = new User();
    account.setUsers(new HashSet<>(Arrays.asList(testUser, otherUser))); // mutable set

    testUser.setAccounts(Collections.singleton(account));

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.deleteUser(1L);

    verify(accountRepository, never()).delete(any());
    verify(userRepository).delete(testUser);
    verify(userCache).evict(1L);
  }

  @Test
  void deleteUser_ShouldThrowWhenUserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
  }

  @Test
  void validatePasswordStrength_ShouldAcceptValidPassword() {
    User validUser = new User();
    validUser.setEmail("valid@example.com");
    validUser.setPhone("+1234567890");
    validUser.setPasswordHash("ValidPass1!");

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByPhone(anyString())).thenReturn(false);
    when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(validUser);

    assertDoesNotThrow(() -> userService.createUser(validUser));
  }

  @Test
  void validatePasswordStrength_ShouldRejectShortPassword() {
    User invalidUser = new User();
    invalidUser.setEmail("valid@example.com");
    invalidUser.setPhone("+1234567890");
    invalidUser.setPasswordHash("Short1!");

    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByPhone(anyString())).thenReturn(false);

    assertThrows(ValidationException.class, () -> userService.createUser(invalidUser));
  }

  @Test
  void validatePasswordStrength_ShouldRejectPasswordWithoutUppercase() {
    testUser.setPasswordHash("lowercase1!");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }

  @Test
  void validatePasswordStrength_ShouldRejectPasswordWithoutDigit() {
    testUser.setPasswordHash("NoDigit!");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }

  @Test
  void validatePasswordStrength_ShouldRejectPasswordWithoutSpecialChar() {
    testUser.setPasswordHash("NoSpecial1");
    assertThrows(ValidationException.class, () -> userService.createUser(testUser));
  }
}