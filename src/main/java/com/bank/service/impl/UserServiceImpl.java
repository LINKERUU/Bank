package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
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
 * Service implementation for user management operations.
 * Provides CRUD functionality for users with password hashing and caching support.
 * Manages user-account relationships and ensures data consistency.
 */
@Service
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final AccountRepository accountRepository;
  private final InMemoryCache<Long, User> userCache;

  /**
   * Constructs a UserServiceImpl with required dependencies.
   *
   * @param userRepository repository for user data access
   * @param passwordService service for password hashing operations
   * @param accountRepository repository for account data access
   * @param userCache in-memory cache for user entities
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
    // Не кэшируем список пользователей
    return userRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findUserById(Long id) {
    User cachedUser = userCache.get(id);
    if (cachedUser != null) {
      return Optional.of(cachedUser);
    }

    Optional<User> user = userRepository.findById(id);
    user.ifPresent(u -> userCache.put(id, u));
    return user;
  }

  @Override
  @Transactional
  public User createUser(User user) {
    user.setPasswordHash(passwordService.hashPassword(user.getPasswordHash()));
    User savedUser = userRepository.save(user);
    userCache.put(savedUser.getId(), savedUser);
    return savedUser;
  }

  @Override
  @Transactional
  public List<User> createUsers(List<User> users) {
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

    updateUserFields(existingUser, updatedUser);

    User savedUser = userRepository.save(existingUser);
    userCache.put(id, savedUser);
    return savedUser;
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    // Удаляем пользователя из связанных аккаунтов
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