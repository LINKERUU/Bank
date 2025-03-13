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
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implementation of the {@link UserService} interface for managing users.
 * Provides methods for retrieving, creating, updating, and deleting users.
 */
@Service
public class UserServiceImpl implements UserService {

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
  public User updateUser(Long id, User user) {
    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found User with id: " + id));

    existingUser.setFirstName(user.getFirstName());
    existingUser.setLastName(user.getLastName());
    existingUser.setEmail(user.getEmail());
    existingUser.setPhone(user.getPhone());

    if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
      existingUser.setPasswordHash(passwordService.hashPassword(user.getPasswordHash()));
    }

    // If account IDs are provided, load them from the database
    if (user.getAccounts() != null) {
      Set<Account> updatedAccounts = user.getAccounts().stream()
              .map(account -> accountRepository.findById(account.getId())
                      .orElseThrow(() -> new ResourceNotFoundException(
                              "Not found User with id: " + id)))
              .collect(Collectors.toSet());

      existingUser.setAccounts(updatedAccounts); // Set the existing accounts
    }

    return userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("Not found User with id: " + id);
    }
    userRepository.deleteById(id);
  }
}