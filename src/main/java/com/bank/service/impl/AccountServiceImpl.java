package com.bank.service.impl;

import com.bank.dto.AccountDto;
import com.bank.dto.CardDto;
import com.bank.dto.TransactionDto;
import com.bank.dto.UserDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.repository.UserRepository;
import com.bank.service.AccountService;
import com.bank.utils.InMemoryCache;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AccountService} that provides business logic for account operations.
 * Handles account creation, retrieval, updating, and deletion with proper validation and caching.
 */
@Service
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final InMemoryCache<Long, AccountDto> accountCache;

  /**
   * Constructs an AccountServiceImpl with required dependencies.
   *
   * @param accountRepository repository for account data access
   * @param cardRepository repository for card data access
   * @param userRepository repository for user data access
   * @param accountCache cache for storing account data
   */
  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
                            CardRepository cardRepository,
                            UserRepository userRepository,
                            InMemoryCache<Long, AccountDto> accountCache) {
    this.accountRepository = accountRepository;
    this.cardRepository = cardRepository;
    this.userRepository = userRepository;
    this.accountCache = accountCache;
  }

  private String generateUniqueAccountNumber() {
    Random random = new Random();
    String accountNumber;

    do {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 20; i++) {
        sb.append(random.nextInt(10));
      }
      accountNumber = sb.toString();
    } while (accountRepository.existsByAccountNumber(accountNumber));

    return accountNumber;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountDto> findByUserEmail(String email) {
    return accountRepository.findByUserEmail(email).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountDto> findAccountsWithCards() {
    return accountRepository.findAccountsWithCards().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AccountDto> findAllAccounts() {
    return accountRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AccountDto> findAccountById(Long id) {
    AccountDto cachedAccount = accountCache.get(id);
    if (cachedAccount != null) {
      return Optional.of(cachedAccount);
    }

    Optional<AccountDto> account = accountRepository.findById(id)
            .map(this::convertToDto);
    account.ifPresent(acc -> accountCache.put(id, acc));
    return account;
  }

  @Override
  @Transactional
  public AccountDto createAccount(AccountDto accountDto) {
    if (accountDto.getUsers() == null || accountDto.getUsers().isEmpty()) {
      throw new ValidationException("The account must be linked to at least one user");
    }

    // Генерация номера счета **до валидации**
    if (accountDto.getAccountNumber() == null || accountDto.getAccountNumber().isEmpty()) {
      accountDto.setAccountNumber(generateUniqueAccountNumber());
    }

    validateAccount(accountDto);

    Account account = convertToEntity(accountDto);
    if (account.getCreatedAt() == null) {
      account.setCreatedAt(LocalDateTime.now());
    }

    Set<User> users = accountDto.getUsers().stream()
            .map(userDto -> userRepository.findById(userDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + userDto.getId())))
            .collect(Collectors.toSet());
    account.setUsers(users);

    Account savedAccount = accountRepository.save(account);
    AccountDto savedAccountDto = convertToDto(savedAccount);
    accountCache.put(savedAccountDto.getId(), savedAccountDto);
    return savedAccountDto;
  }


  @Override
  @Transactional
  public List<AccountDto> createAccounts(List<AccountDto> accountDtos) {
    List<String> validationErrors = accountDtos.stream()
            .map(account -> {
              try {
                validateAccount(account);
                if (account.getUsers() == null || account.getUsers().isEmpty()) {
                  return "Account " + account.getAccountNumber()
                          + ": must be linked to at least one user";
                }
                return null;
              } catch (ValidationException e) {
                return "Account " + account.getAccountNumber() + ": " + e.getMessage();
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Batch validation failed:\n"
              + String.join("\n", validationErrors));
    }

    List<Account> accounts = accountDtos.stream()
            .map(dto -> {
              Account account = convertToEntity(dto);
              if (account.getCreatedAt() == null) {
                account.setCreatedAt(LocalDateTime.now());
              }

              Set<User> users = dto.getUsers().stream()
                      .map(userDto -> userRepository.findById(userDto.getId())
                              .orElseThrow(() -> new ResourceNotFoundException(
                                      "User not found with id: " + userDto.getId())))
                      .collect(Collectors.toSet());
              account.setUsers(users);

              return account;
            })
            .collect(Collectors.toList());

    List<AccountDto> savedAccounts = accountRepository.saveAll(accounts)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

    savedAccounts.forEach(account -> accountCache.put(account.getId(), account));
    return savedAccounts;
  }

  /**
   * Validates account data before creation or update.
   *
   * @param accountDto the account data to validate
   * @throws ValidationException if validation fails
   */
  private void validateAccount(AccountDto accountDto) {
    if (accountDto.getAccountNumber() == null
            || accountDto.getAccountNumber().length() < 10
            || accountDto.getAccountNumber().length() > 20) {
      throw new ValidationException(
              "Account number must be between 10 and 20 characters");
    }
    if (!accountDto.getAccountNumber().matches("^\\d+$")) {
      throw new ValidationException("Account number must contain only digits");
    }
    if (accountDto.getBalance() == null || accountDto.getBalance() < 0) {
      throw new ValidationException("Balance cannot be negative");
    }
  }

  @Override
  @Transactional
  public void deleteAccount(Long id) {
    Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account with ID " + id + " not found"));

    if (account.getCards() != null && !account.getCards().isEmpty()) {
      cardRepository.deleteAll(account.getCards());
    }

    accountRepository.delete(account);
    accountCache.evict(id);
  }

  @Override
  @Transactional
  public AccountDto updateAccount(Long id, AccountDto updatedAccountDto) {
    Account existingAccount = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account with ID " + id + " not found"));

    if (updatedAccountDto.getAccountNumber() != null) {
      existingAccount.setAccountNumber(updatedAccountDto.getAccountNumber());
    }
    if (updatedAccountDto.getBalance() != null) {
      existingAccount.setBalance(updatedAccountDto.getBalance());
    }

    if (updatedAccountDto.getUsers() != null) {
      updateAccountUsers(existingAccount, updatedAccountDto.getUsers());
    }

    Account savedAccount = accountRepository.save(existingAccount);
    AccountDto savedAccountDto = convertToDto(savedAccount);
    accountCache.put(id, savedAccountDto);
    return savedAccountDto;
  }

  @Override
  @Transactional
  public AccountDto updateAccountUsers(Long accountId, Set<Long> userIds) {
    Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account not found with id: " + accountId));

    if (userIds == null || userIds.isEmpty()) {
      throw new ValidationException("Account must have at least one user");
    }

    Set<User> users = userIds.stream()
            .map(userId -> userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + userId)))
            .collect(Collectors.toSet());

    account.setUsers(users);
    Account updatedAccount = accountRepository.save(account);
    return convertToDto(updatedAccount);
  }

  private void updateAccountUsers(Account account, Set<UserDto> userDtos) {
    if (userDtos.isEmpty()) {
      throw new ValidationException("Account must have at least one user");
    }

    Set<User> newUsers = userDtos.stream()
            .map(userDto -> userRepository.findById(userDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + userDto.getId())))
            .collect(Collectors.toSet());

    account.getUsers().clear();
    account.getUsers().addAll(newUsers);
  }

  private AccountDto convertToDto(Account account) {
    AccountDto dto = new AccountDto();
    dto.setId(account.getId());
    dto.setAccountNumber(account.getAccountNumber());
    dto.setBalance(account.getBalance());
    dto.setCreatedAt(account.getCreatedAt() != null
            ? account.getCreatedAt()
            : LocalDateTime.now());

    if (account.getCards() != null) {
      Set<CardDto> cardDtos = account.getCards().stream()
              .map(card -> {
                CardDto cardDto = new CardDto();
                cardDto.setId(card.getId());
                cardDto.setCardNumber(card.getCardNumber());
                cardDto.setExpirationDate(card.getExpirationDate());
                cardDto.setCvv(card.getCvv());
                cardDto.setAccountId(card.getAccount().getId());
                cardDto.setAccountNumber(card.getAccount().getAccountNumber());
                return cardDto;
              })
              .collect(Collectors.toSet());
      dto.setCards(cardDtos);
    }

    if (account.getUsers() != null) {
      Set<UserDto> userDtos = account.getUsers().stream()
              .map(user -> {
                UserDto userDto = new UserDto();
                userDto.setId(user.getId());
                userDto.setFirstName(user.getFirstName());
                userDto.setLastName(user.getLastName());
                userDto.setEmail(user.getEmail());
                userDto.setPhone(user.getPhone());
                userDto.setCreatedAt(user.getCreatedAt());
                return userDto;
              })
              .collect(Collectors.toSet());
      dto.setUsers(userDtos);
    }

    if (account.getTransactions() != null) {
      Set<TransactionDto> transactionDtos = account.getTransactions().stream()
              .map(transaction -> {
                TransactionDto transactionDto = new TransactionDto();
                transactionDto.setId(transaction.getId());
                transactionDto.setAmount(transaction.getAmount());
                transactionDto.setTransactionDate(transaction.getTransactionDate());
                transactionDto.setTransactionType(transaction.getTransactionType());
                transactionDto.setDescription(transaction.getDescription());
                return transactionDto;
              })
              .collect(Collectors.toSet());
      dto.setTransactions(transactionDtos);
    }

    return dto;
  }

  private Account convertToEntity(AccountDto dto) {
    Account account = new Account();
    account.setId(dto.getId());
    account.setAccountNumber(dto.getAccountNumber());
    account.setBalance(dto.getBalance());
    account.setCreatedAt(dto.getCreatedAt() != null
            ? dto.getCreatedAt()
            : LocalDateTime.now());
    return account;
  }
}