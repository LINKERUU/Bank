package com.bank.service.impl;

import com.bank.dto.TransactionDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.TransactionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for transaction operations.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private static final String TRANSACTION_TYPE_DEBIT = "credit";
  private static final String TRANSACTION_TYPE_CREDIT = "debit";

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  /**
   * Constructs a new TransactionServiceImpl.
   *
   * @param transactionRepository the transaction repository
   * @param accountRepository the account repository
   */
  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  /**
   * Retrieves all transactions.
   *
   * @return list of transaction DTOs
   */
  @Override
  @Transactional(readOnly = true)
  public List<TransactionDto> findAllTransactions() {
    return transactionRepository.findAll().stream()
            .map(transaction -> {
              TransactionDto dto = new TransactionDto();
              dto.setId(transaction.getId());
              dto.setAmount(transaction.getAmount());
              dto.setTransactionType(transaction.getTransactionType());
              dto.setDescription(transaction.getDescription());
              dto.setTransactionDate(transaction.getTransactionDate());
              dto.setAccountId(transaction.getAccount().getId());
              dto.setAccountNumber(transaction.getAccount().getAccountNumber());
              return dto;
            })
            .collect(Collectors.toList());
  }

  /**
   * Finds a transaction by ID.
   *
   * @param id the transaction ID
   * @return optional containing the transaction DTO if found
   * @throws ValidationException if the ID is invalid
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<TransactionDto> findTransactionById(Long id) {
    if (id == null || id <= 0) {
      throw new ValidationException("Invalid transaction ID");
    }
    return transactionRepository.findById(id)
            .map(transaction -> {
              TransactionDto dto = new TransactionDto();
              dto.setId(transaction.getId());
              dto.setAmount(transaction.getAmount());
              dto.setTransactionType(transaction.getTransactionType());
              dto.setDescription(transaction.getDescription());
              dto.setTransactionDate(transaction.getTransactionDate());
              dto.setAccountId(transaction.getAccount().getId());
              dto.setAccountNumber(transaction.getAccount().getAccountNumber());
              return dto;
            });
  }

  /**
   * Creates a new transaction.
   *
   * @param transactionDto the transaction data
   * @return the created transaction DTO
   * @throws ValidationException if the transaction data is invalid
   * @throws ResourceNotFoundException if the account is not found
   */
  @Override
  @Transactional
  public TransactionDto createTransaction(TransactionDto transactionDto) {
    validateTransaction(transactionDto);

    var account = accountRepository.findById(transactionDto.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account not found with ID: " + transactionDto.getAccountId()));

    var transaction = new com.bank.model.Transaction();
    transaction.setAmount(transactionDto.getAmount());
    transaction.setTransactionType(transactionDto.getTransactionType());
    transaction.setDescription(transactionDto.getDescription());
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setAccount(account);

    processTransaction(transaction, account);

    accountRepository.save(account);
    var savedTransaction = transactionRepository.save(transaction);

    transactionDto.setId(savedTransaction.getId());
    transactionDto.setTransactionDate(savedTransaction.getTransactionDate());
    transactionDto.setAccountNumber(account.getAccountNumber());
    return transactionDto;
  }

  /**
   * Updates an existing transaction.
   *
   * @param id the transaction ID
   * @param updatedTransactionDto the updated transaction data
   * @return the updated transaction DTO
   * @throws ValidationException if the ID or transaction data is invalid
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @Override
  @Transactional
  public TransactionDto updateTransaction(Long id, TransactionDto updatedTransactionDto) {
    validateTransactionId(id);
    validateTransaction(updatedTransactionDto);

    var existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with ID: " + id));

    updateAccountBalance(existingTransaction, updatedTransactionDto);
    updateTransactionFields(existingTransaction, updatedTransactionDto);

    var updatedTransaction = transactionRepository.save(existingTransaction);

    TransactionDto resultDto = new TransactionDto();
    resultDto.setId(updatedTransaction.getId());
    resultDto.setAmount(updatedTransaction.getAmount());
    resultDto.setTransactionType(updatedTransaction.getTransactionType());
    resultDto.setDescription(updatedTransaction.getDescription());
    resultDto.setTransactionDate(updatedTransaction.getTransactionDate());
    resultDto.setAccountId(updatedTransaction.getAccount().getId());
    resultDto.setAccountNumber(updatedTransaction.getAccount().getAccountNumber());

    return resultDto;
  }

  /**
   * Deletes a transaction.
   *
   * @param id the transaction ID
   * @throws ValidationException if the ID is invalid
   * @throws ResourceNotFoundException if the transaction is not found
   */
  @Override
  @Transactional
  public void deleteTransaction(Long id) {
    validateTransactionId(id);

    var transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found with ID: " + id));

    revertAccountBalance(transaction);
    transactionRepository.deleteById(id);
  }

  private void validateTransactionId(Long id) {
    if (id == null || id <= 0) {
      throw new ValidationException("Invalid transaction ID");
    }
  }

  private void validateTransaction(TransactionDto transactionDto) {
    if (transactionDto == null) {
      throw new ValidationException("Transaction cannot be null");
    }

    if (transactionDto.getAmount() == null || transactionDto.getAmount() <= 0) {
      throw new ValidationException("Transaction amount must be positive");
    }

    if (transactionDto.getTransactionType() == null
            || !(TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transactionDto.getTransactionType())
            || TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transactionDto.getTransactionType()))) {
      throw new ValidationException("Transaction type must be 'credit' or 'debit'");
    }

    if (transactionDto.getAccountId() == null) {
      throw new ValidationException("Account ID cannot be null");
    }
  }

  private void processTransaction(com.bank.model.Transaction transaction,
                                  com.bank.model.Account account) {
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transaction.getTransactionType())) {
      account.setBalance(account.getBalance() + transaction.getAmount());
    } else {
      if (account.getBalance() < transaction.getAmount()) {
        throw new ValidationException("Insufficient funds for debit transaction");
      }
      account.setBalance(account.getBalance() - transaction.getAmount());
    }
    transaction.setAccount(account);
  }

  private void updateAccountBalance(com.bank.model.Transaction existing,
                                    TransactionDto updatedDto) {
    var account = existing.getAccount();

    // Revert existing transaction effect
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(existing.getTransactionType())) {
      account.setBalance(account.getBalance() - existing.getAmount());
    } else {
      account.setBalance(account.getBalance() + existing.getAmount());
    }

    // Apply new transaction effect
    if (updatedDto.getAmount() != null && updatedDto.getTransactionType() != null) {
      if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(updatedDto.getTransactionType())) {
        account.setBalance(account.getBalance() + updatedDto.getAmount());
      } else {
        if (account.getBalance() < updatedDto.getAmount()) {
          throw new ValidationException("Insufficient funds for debit transaction");
        }
        account.setBalance(account.getBalance() - updatedDto.getAmount());
      }
    }

    accountRepository.save(account);
  }

  private void updateTransactionFields(com.bank.model.Transaction existing,
                                       TransactionDto updatedDto) {
    if (updatedDto.getAmount() != null) {
      existing.setAmount(updatedDto.getAmount());
    }
    if (updatedDto.getTransactionType() != null) {
      existing.setTransactionType(updatedDto.getTransactionType());
    }
    if (updatedDto.getDescription() != null) {
      existing.setDescription(updatedDto.getDescription());
    }
    if (updatedDto.getAccountId() != null) {
      var newAccount = accountRepository.findById(updatedDto.getAccountId())
              .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
      existing.setAccount(newAccount);
    }
  }

  private void revertAccountBalance(com.bank.model.Transaction transaction) {
    var account = transaction.getAccount();

    if (TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transaction.getTransactionType())) {
      if (account.getBalance() < transaction.getAmount()) {
        throw new ValidationException("Cannot revert transaction - insufficient funds");
      }
      account.setBalance(account.getBalance() + transaction.getAmount());
    } else {
      account.setBalance(account.getBalance() - transaction.getAmount());
    }

    accountRepository.save(account);
  }
}