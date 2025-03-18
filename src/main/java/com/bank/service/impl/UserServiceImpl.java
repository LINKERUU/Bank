package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.PasswordService;
import com.bank.service.UserService;
import java.util.List;
import java.util.Optional;
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

  /**
   * Constructs a new UserServiceImpl with the specified repositories and services.
   *
   * @param userRepository the repository for managing users
   * @param passwordService the service for password hashing
   * @param accountRepository the repository for managing accounts
   */
  public UserServiceImpl(UserRepository userRepository, PasswordService passwordService,
                         AccountRepository accountRepository) {
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.accountRepository = accountRepository;
  }

  @Override
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public Optional<User> findUserById(Long id) {
    return userRepository.findById(id);
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
    // Находим существующего пользователя по ID
    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));

    // Обновляем только те поля, которые были переданы в запросе
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

    // Не обновляем поле accounts, чтобы сохранить существующие связи

    // Сохраняем обновлённого пользователя
    return userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID "
                    + userId + " не найден."));

    // Удаляем пользователя из всех связанных аккаунтов
    for (Account account : user.getAccounts()) {
      account.getUsers().remove(user);
      if (account.getUsers().isEmpty()) {
        // Если у аккаунта больше нет пользователей, удаляем его
        accountRepository.delete(account);
      }
    }

    // Удаляем самого пользователя
    userRepository.delete(user);
  }
}