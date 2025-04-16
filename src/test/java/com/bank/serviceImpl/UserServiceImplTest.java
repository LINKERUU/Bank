package com.bank.serviceImpl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.serviceImpl.impl.UserServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

  private User user;
  private Account account;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john.doe@example.com");
    user.setPhone("+1234567890");
    user.setPasswordHash("Password123!");

    account = new Account();
    account.setId(1L);
    account.setUsers((java.util.Set<User>) Collections.singletonList(user));
  }

  @Test
  void findAllUsers_ShouldReturnAllUsers() {
    // Arrange
    User user2 = new User();
    user2.setId(2L);
    List<User> expectedUsers = Arrays.asList(user, user2);
    when(userRepository.findAll()).thenReturn(expectedUsers);

    // Act
    List<User> result = userService.findAllUsers();

    // Assert
    assertEquals(2, result.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void findUserById_WithCachedUser_ShouldReturnFromCache() {
    // Arrange
    when(userCache.get(1L)).thenReturn(user);

    // Act
    Optional<User> result = userService.findUserById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verify(userCache, times(1)).get(1L);
    verify(userRepository, never()).findById(anyLong());
  }

  @Test
  void findUserById_WithNonCachedUser_ShouldFetchFromRepository() {
    // Arrange
    when(userCache.get(1L)).thenReturn(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act
    Optional<User> result = userService.findUserById(1L);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verify(userCache, times(1)).get(1L);
    verify(userRepository, times(1)).findById(1L);
    verify(userCache, times(1)).put(1L, user);
  }

  @Test
  void createUser_WithValidData_ShouldCreateUser() {
    // Arrange
    when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    User result = userService.createUser(user);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userRepository, times(1)).save(any(User.class));
    verify(userCache, times(1)).put(1L, user);
    verify(passwordService, times(1)).hashPassword(anyString());
  }

  @Test
  void createUser_WithInvalidEmail_ShouldThrowException() {
    // Arrange
    user.setEmail(null);

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertEquals("Email is required", exception.getMessage());
  }

  @Test
  void createUser_WithWeakPassword_ShouldThrowException() {
    // Arrange
    user.setPasswordHash("weak");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertTrue(exception.getMessage().contains("Password must be at least 8 characters long"));
  }

  @Test
  void createUsers_WithValidList_ShouldCreateUsers() {
    // Arrange
    User user2 = new User();
    user2.setId(2L);
    user2.setEmail("jane@example.com");
    user2.setPhone("+987654321");
    user2.setPasswordHash("Password123!");
    List<User> users = Arrays.asList(user, user2);

    when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
    when(userRepository.saveAll(anyList())).thenReturn(users);

    // Act
    List<User> result = userService.createUsers(users);

    // Assert
    assertEquals(2, result.size());
    verify(userRepository, times(1)).saveAll(anyList());
    verify(passwordService, times(2)).hashPassword(anyString());
    verify(userCache, times(1)).put(1L, user);
    verify(userCache, times(1)).put(2L, user2);
  }

  @Test
  void updateUser_WithValidData_ShouldUpdateUser() {
    // Arrange
    User updatedUser = new User();
    updatedUser.setFirstName("John Updated");
    updatedUser.setLastName("Doe Updated");
    updatedUser.setEmail("john.updated@example.com");
    updatedUser.setPasswordHash("NewPassword123!");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordService.hashPassword(anyString())).thenReturn("hashedNewPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    User result = userService.updateUser(1L, updatedUser);

    // Assert
    assertNotNull(result);
    assertEquals("John Updated", result.getFirstName());
    assertEquals("Doe Updated", result.getLastName());
    assertEquals("john.updated@example.com", result.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
    verify(userCache, times(1)).put(1L, user);
  }

  @Test
  void updateUser_WithNonExistentId_ShouldThrowException() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> userService.updateUser(99L, user));
    assertEquals("User not found with id: 99", exception.getMessage());
  }

  @Test
  void deleteUser_WithValidId_ShouldDeleteUser() {
    // Arrange
    user.setAccounts((java.util.Set<Account>) Collections.singletonList(account));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(accountRepository.save(any(Account.class))).thenReturn(account);

    // Act
    userService.deleteUser(1L);

    // Assert
    verify(userRepository, times(1)).delete(user);
    verify(userCache, times(1)).evict(1L);
    verify(accountRepository, times(1)).save(account);
  }

  @Test
  void deleteUser_WithNonExistentId_ShouldThrowException() {
    // Arrange
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> userService.deleteUser(99L));
    assertEquals("User not found with ID: 99", exception.getMessage());
  }

  @Test
  void validatePasswordStrength_WithValidPassword_ShouldNotThrowException() {
    // Act & Assert
    assertDoesNotThrow(() -> userService.createUser(user));
  }

  @Test
  void validatePasswordStrength_WithShortPassword_ShouldThrowException() {
    // Arrange
    user.setPasswordHash("Short1!");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertEquals("Password must be at least 8 characters long", exception.getMessage());
  }

  @Test
  void validatePasswordStrength_WithoutUppercase_ShouldThrowException() {
    // Arrange
    user.setPasswordHash("lowercase1!");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertEquals("Password must contain at least one uppercase letter", exception.getMessage());
  }

  @Test
  void validatePasswordStrength_WithoutDigit_ShouldThrowException() {
    // Arrange
    user.setPasswordHash("NoDigits!");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertEquals("Password must contain at least one digit", exception.getMessage());
  }

  @Test
  void validatePasswordStrength_WithoutSpecialChar_ShouldThrowException() {
    // Arrange
    user.setPasswordHash("NoSpecial1");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> userService.createUser(user));
    assertEquals("Password must contain at least one special character", exception.getMessage());
  }
}