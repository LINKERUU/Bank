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
 * Implementation of the {@link UserService} interface for managing users.
 * Provides methods for retrieving, creating, updating, and deleting users.
 */
@Service
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND_MESSAGE = "Not found User with id: ";

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final AccountRepository accountRepository;
  private final InMemoryCache<String, List<User>> userCache; // Кэш для списка пользователей
  private final InMemoryCache<Long, User> userByIdCache; // Кэш для пользователя по ID

  /**
   * Constructor for UserServiceImpl.
   *
   * @param userRepository the user repository
   * @param passwordService the password hashing service
   * @param accountRepository the account repository
   * @param userCache cache for user lists
   * @param userByIdCache cache for users by ID
   */
  @Autowired
  public UserServiceImpl(UserRepository userRepository, PasswordService passwordService,
                         AccountRepository accountRepository,
                         InMemoryCache<String, List<User>> userCache,
                         InMemoryCache<Long, User> userByIdCache) {
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.accountRepository = accountRepository;
    this.userCache = userCache;
    this.userByIdCache = userByIdCache;
  }

  @Override
  public List<User> findAllUsers() {
    String cacheKey = "all_users";
    List<User> cachedUsers = userCache.get(cacheKey);
    if (cachedUsers != null) {
      return cachedUsers;
    }

    List<User> users = userRepository.findAll();
    userCache.put(cacheKey, users);
    return users;
  }

  @Override
  public Optional<User> findUserById(Long id) {
    User cachedUser = userByIdCache.get(id);
    if (cachedUser != null) {
      return Optional.of(cachedUser);
    }

    Optional<User> user = userRepository.findById(id);
    user.ifPresent(u -> userByIdCache.put(id, u));
    return user;
  }

  @Override
  @Transactional
  public User createUser(User user) {
    // Hash the password before saving
    String hashedPassword = passwordService.hashPassword(user.getPasswordHash());
    user.setPasswordHash(hashedPassword);
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public List<User> createUsers(List<User> users) {
    users.forEach(user -> user.setPasswordHash(
            passwordService.hashPassword(user.getPasswordHash())));
    return userRepository.saveAll(users);
  }

  @Override
  @Transactional
  public User updateUser(Long id, User updatedUser) {

    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));


    if (updatedUser.getFirstName() != null) {
      existingUser.setFirstName(updatedUser.getFirstName());
    }
    if (updatedUser.getLastName() != null) {
      existingUser.setLastName(updatedUser.getLastName());
    }
    if (updatedUser.getEmail() != null) {
      existingUser.setEmail(updatedUser.getEmail());
    }
    if (updatedUser.getPhone() != null) {
      existingUser.setPhone(updatedUser.getPhone());
    }
    if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
      existingUser.setPasswordHash(passwordService.hashPassword(updatedUser.getPasswordHash()));
    }

    return userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID "
                    + userId + " не найден."));

    for (Account account : user.getAccounts()) {
      account.getUsers().remove(user);
      if (account.getUsers().isEmpty()) {
        accountRepository.delete(account);
      }
    }

    userRepository.delete(user);

    userByIdCache.evict(userId);
    userCache.clear();
  }
}