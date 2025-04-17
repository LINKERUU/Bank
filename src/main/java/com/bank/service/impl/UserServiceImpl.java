package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.PasswordService;
import com.bank.service.UserService;
import com.bank.utils.InMemoryCache;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the UserService interface providing CRUD operations for User entities.
 * This service handles user management including creation, retrieval, update, and deletion,
 * with proper validation and caching mechanisms.
 */
@Service
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final AccountRepository accountRepository;
  private final InMemoryCache<Long, User> userCache;

  /**
   * Constructs a new UserServiceImpl with required repositories.
   *
   * @param userRepository repository for transaction data access
   */
  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         PasswordService passwordService,
                         AccountRepository accountRepository,
                         InMemoryCache<Long, User> userCache) {
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.accountRepository = accountRepository;
    this.userCache = userCache;
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public Optional<User> findUserById(Long id) {
    // 1. Проверка кэша
    User cachedUser = userCache.get(id);
    if (cachedUser != null) {
      return Optional.of(cachedUser); // Возвращаем из кэша
    }

    // 2. Если нет в кэше, ищем в репозитории
    Optional<User> user = userRepository.findById(id);
    user.ifPresent(u -> userCache.put(id, u)); // Кэшируем найденного пользователя

    return user;
  }

  @Override
  @Transactional
  public User createUser(User user) {
    validateUser(user);
    user.setPasswordHash(passwordService.hashPassword(user.getPasswordHash()));
    User savedUser = userRepository.save(user);
    userCache.put(savedUser.getId(), savedUser);
    return savedUser;
  }

  @Override
  @Transactional
  public List<User> createUsers(List<User> users) {
    users.forEach(this::validateUser);
    users.forEach(user ->
            user.setPasswordHash(passwordService.hashPassword(user.getPasswordHash()))
    );
    List<User> savedUsers = userRepository.saveAll(users);
    savedUsers.forEach(user -> userCache.put(user.getId(), user));
    return savedUsers;
  }

  @Override
  @Transactional
  public User updateUser(Long id, User updatedUser) {
    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));

    validateUserForUpdate(updatedUser, existingUser);
    updateUserFields(existingUser, updatedUser);

    User savedUser = userRepository.save(existingUser);
    userCache.put(id, savedUser);
    return savedUser;
  }

  private void validateUser(User user) {
    if (user == null) {
      throw new ValidationException("User cannot be null");
    }

    if (user.getEmail() == null || user.getEmail().isBlank()) {
      throw new ValidationException("Email is required");
    }

    if (user.getPhone() == null || user.getPhone().isBlank()) {
      throw new ValidationException("Phone is required");
    }

    if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
      throw new ValidationException("Password is required");
    }

    if (userRepository.existsByEmail(user.getEmail())) {
      throw new ValidationException("User with this email already exists");
    }

    if (userRepository.existsByPhone(user.getPhone())) {
      throw new ValidationException("User with this phone already exists");
    }

    validatePasswordStrength(user.getPasswordHash());
  }

  private void validateUserForUpdate(User updatedUser, User existingUser) {
    if (updatedUser.getEmail() != null
            && !updatedUser.getEmail().equals(existingUser.getEmail())
            && userRepository.existsByEmail(updatedUser.getEmail())) {  // Changed from existsById
      throw new ValidationException("User with this email already exists");
    }

    if (updatedUser.getPhone() != null
            && !updatedUser.getPhone().equals(existingUser.getPhone())
            && userRepository.existsByPhone(updatedUser.getPhone())) {  // Changed from existsById
      throw new ValidationException("User with this phone already exists");
    }

    if (updatedUser.getPasswordHash() != null) {
      validatePasswordStrength(updatedUser.getPasswordHash());
    }
  }

  private void validatePasswordStrength(String password) {
    // Check length first
    if (password.length() < 8) {
      throw new ValidationException("Password must be at least 8 characters long");
    }

    // Check for at least one uppercase letter (English and Cyrillic)
    if (!containsUppercase(password)) {
      throw new ValidationException("Password must contain at least one uppercase letter");
    }

    // Check for at least one digit (using \\d)
    if (!containsDigit(password)) {
      throw new ValidationException("Password must contain at least one digit");
    }

    // Check for at least one special character
    if (!containsSpecialChar(password)) {
      throw new ValidationException("Password must contain at least one special character");
    }
  }

  // Optimized regex checks with no catastrophic backtracking potential
  private boolean containsUppercase(String password) {
    // Simple character check without regex
    for (char c : password.toCharArray()) {
      if (Character.isUpperCase(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsDigit(String password) {
    // Simple character check without regex
    for (char c : password.toCharArray()) {
      if (Character.isDigit(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsSpecialChar(String password) {
    // Defined set of special characters
    String specialChars = "!@#$%^&*()_+-=[]{};':\"\\|,.<>?";
    for (char c : password.toCharArray()) {
      if (specialChars.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    for (Account account : user.getAccounts()) {
      account.getUsers().remove(user);
      if (account.getUsers().isEmpty()) {
        accountRepository.delete(account);
      }
    }

    userRepository.delete(user);
    userCache.evict(userId);
  }

  private void updateUserFields(User existing, User updated) {
    if (updated.getFirstName() != null) {
      existing.setFirstName(updated.getFirstName());
    }
    if (updated.getLastName() != null) {
      existing.setLastName(updated.getLastName());
    }
    if (updated.getEmail() != null) {
      existing.setEmail(updated.getEmail());
    }
    if (updated.getPhone() != null) {
      existing.setPhone(updated.getPhone());
    }
    if (updated.getPasswordHash() != null && !updated.getPasswordHash().isEmpty()) {
      existing.setPasswordHash(passwordService.hashPassword(updated.getPasswordHash()));
    }
  }
}