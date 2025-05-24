package com.bank.service.impl;

import com.bank.dto.AccountDto;
import com.bank.dto.UserDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import com.bank.service.PasswordService;
import com.bank.service.UserService;
import com.bank.utils.InMemoryCache;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  private static final String USER_NOT_FOUND_MESSAGE = "User not found with id: ";

  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final AccountRepository accountRepository;
  private final InMemoryCache<Long, User> userCache;

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
  public List<UserDto> findAllUsers() {
    return userRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> findUserById(Long id) {
    User cachedUser = userCache.get(id);
    if (cachedUser != null) {
      return Optional.of(convertToDto(cachedUser));
    }

    return userRepository.findById(id)
            .map(user -> {
              userCache.put(id, user);
              return convertToDto(user);
            });
  }

  @Override
  @Transactional
  public UserDto createUser(UserDto userDTO) {
    User user = convertToEntity(userDTO);
    validateUser(user);
    user.setPasswordHash(passwordService.hashPassword(user.getPasswordHash()));
    User savedUser = userRepository.save(user);
    userCache.put(savedUser.getId(), savedUser);
    return convertToDto(savedUser);
  }



  @Override
  @Transactional
  public List<UserDto> createUsers(List<UserDto> usersDTO) {
    List<User> users = usersDTO.stream()
            .map(this::convertToEntity)
            .peek(this::validateUser)
            .peek(user -> user.setPasswordHash(passwordService.hashPassword(user.getPasswordHash())))
            .collect(Collectors.toList());

    List<User> savedUsers = userRepository.saveAll(users);
    savedUsers.forEach(user -> userCache.put(user.getId(), user));
    return savedUsers.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserDto updateUser(Long id, UserDto userDTO) {
    User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));

    User updatedUser = convertToEntity(userDTO);
    validateUserForUpdate(updatedUser, existingUser);
    updateUserFields(existingUser, updatedUser);

    User savedUser = userRepository.save(existingUser);
    userCache.put(id, savedUser);
    return convertToDto(savedUser);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    for (Account account : user.getAccounts()) {
      // Убедимся, что createdAt установлен
      if (account.getCreatedAt() == null) {
        account.setCreatedAt(LocalDateTime.now());
      }
      account.getUsers().remove(user);
      if (account.getUsers().isEmpty()) {
        accountRepository.delete(account);
      }
    }

    userRepository.delete(user);
    userCache.evict(userId);
  }

  private UserDto convertToDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setEmail(user.getEmail());
    userDto.setPhone(user.getPhone());
    userDto.setCreatedAt(user.getCreatedAt());

    if (user.getAccounts() != null) {
      Set<AccountDto> accountDtos = user.getAccounts().stream()
              .map(this::convertAccountToDto)
              .collect(Collectors.toSet());
      userDto.setAccounts(accountDtos);
    }

    return userDto;
  }

  private AccountDto convertAccountToDto(Account account) {
    AccountDto accountDto = new AccountDto();
    accountDto.setId(account.getId());
    accountDto.setAccountNumber(account.getAccountNumber());
    accountDto.setBalance(account.getBalance());
    accountDto.setCreatedAt(account.getCreatedAt() != null ?
            account.getCreatedAt() :
            LocalDateTime.now());
    return accountDto;
  }

  private User convertToEntity(UserDto userDto) {
    User user = new User();
    user.setId(userDto.getId());
    user.setFirstName(userDto.getFirstName());
    user.setLastName(userDto.getLastName());
    user.setEmail(userDto.getEmail());
    user.setPhone(userDto.getPhone());
    user.setPasswordHash(userDto.getPasswordHash());
    user.setCreatedAt(userDto.getCreatedAt() != null ?
            userDto.getCreatedAt() :
            LocalDateTime.now());
    return user;
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
            && userRepository.existsByEmail(updatedUser.getEmail())) {
      throw new ValidationException("User with this email already exists");
    }

    if (updatedUser.getPhone() != null
            && !updatedUser.getPhone().equals(existingUser.getPhone())
            && userRepository.existsByPhone(updatedUser.getPhone())) {
      throw new ValidationException("User with this phone already exists");
    }

    if (updatedUser.getPasswordHash() != null) {
      validatePasswordStrength(updatedUser.getPasswordHash());
    }
  }

  private void validatePasswordStrength(String password) {
    if (password.length() < 8) {
      throw new ValidationException("Password must be at least 8 characters long");
    }

    if (!containsUppercase(password)) {
      throw new ValidationException("Password must contain at least one uppercase letter");
    }

    if (!containsDigit(password)) {
      throw new ValidationException("Password must contain at least one digit");
    }

    if (!containsSpecialChar(password)) {
      throw new ValidationException("Password must contain at least one special character");
    }
  }

  private boolean containsUppercase(String password) {
    for (char c : password.toCharArray()) {
      if (Character.isUpperCase(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsDigit(String password) {
    for (char c : password.toCharArray()) {
      if (Character.isDigit(c)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsSpecialChar(String password) {
    String specialChars = "!@#$%^&*()_+-=[]{};':\"\\|,.<>?";
    for (char c : password.toCharArray()) {
      if (specialChars.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
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