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

@Service
public class TransactionServiceImpl implements TransactionService {

  private static final String TRANSACTION_TYPE_DEBIT = "credit";
  private static final String TRANSACTION_TYPE_CREDIT = "debit";

  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  @Autowired
  public TransactionServiceImpl(TransactionRepository transactionRepository,
                                AccountRepository accountRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

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

  @Override
  @Transactional
  public TransactionDto createTransaction(TransactionDto transactionDTO) {
    validateTransaction(transactionDTO);

    var account = accountRepository.findById(transactionDTO.getAccountId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Account not found with ID: " + transactionDTO.getAccountId()));

    var transaction = new com.bank.model.Transaction();
    transaction.setAmount(transactionDTO.getAmount());
    transaction.setTransactionType(transactionDTO.getTransactionType());
    transaction.setDescription(transactionDTO.getDescription());
    transaction.setTransactionDate(LocalDateTime.now());
    transaction.setAccount(account);

    processTransaction(transaction, account);

    accountRepository.save(account);
    var savedTransaction = transactionRepository.save(transaction);

    transactionDTO.setId(savedTransaction.getId());
    transactionDTO.setTransactionDate(savedTransaction.getTransactionDate());
    transactionDTO.setAccountNumber(account.getAccountNumber());
    return transactionDTO;
  }

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

    TransactionDto resultDTO = new TransactionDto();
    resultDTO.setId(updatedTransaction.getId());
    resultDTO.setAmount(updatedTransaction.getAmount());
    resultDTO.setTransactionType(updatedTransaction.getTransactionType());
    resultDTO.setDescription(updatedTransaction.getDescription());
    resultDTO.setTransactionDate(updatedTransaction.getTransactionDate());
    resultDTO.setAccountId(updatedTransaction.getAccount().getId());
    resultDTO.setAccountNumber(updatedTransaction.getAccount().getAccountNumber());

    return resultDTO;
  }

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

  private void validateTransaction(TransactionDto transactionDTO) {
    if (transactionDTO == null) {
      throw new ValidationException("Transaction cannot be null");
    }

    if (transactionDTO.getAmount() == null || transactionDTO.getAmount() <= 0) {
      throw new ValidationException("Transaction amount must be positive");
    }

    if (transactionDTO.getTransactionType() == null
            || !(TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(transactionDTO.getTransactionType())
            || TRANSACTION_TYPE_CREDIT.equalsIgnoreCase(transactionDTO.getTransactionType()))) {
      throw new ValidationException("Transaction type must be 'credit' or 'debit'");
    }

    if (transactionDTO.getAccountId() == null) {
      throw new ValidationException("Account ID cannot be null");
    }
  }

  private void processTransaction(com.bank.model.Transaction transaction, com.bank.model.Account account) {
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

  private void updateAccountBalance(com.bank.model.Transaction existing, TransactionDto updatedDTO) {
    var account = existing.getAccount();

    // Revert existing transaction effect
    if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(existing.getTransactionType())) {
      account.setBalance(account.getBalance() - existing.getAmount());
    } else {
      account.setBalance(account.getBalance() + existing.getAmount());
    }

    // Apply new transaction effect
    if (updatedDTO.getAmount() != null && updatedDTO.getTransactionType() != null) {
      if (TRANSACTION_TYPE_DEBIT.equalsIgnoreCase(updatedDTO.getTransactionType())) {
        account.setBalance(account.getBalance() + updatedDTO.getAmount());
      } else {
        if (account.getBalance() < updatedDTO.getAmount()) {
          throw new ValidationException("Insufficient funds for debit transaction");
        }
        account.setBalance(account.getBalance() - updatedDTO.getAmount());
      }
    }

    accountRepository.save(account);
  }

  private void updateTransactionFields(com.bank.model.Transaction existing, TransactionDto updatedDTO) {
    if (updatedDTO.getAmount() != null) {
      existing.setAmount(updatedDTO.getAmount());
    }
    if (updatedDTO.getTransactionType() != null) {
      existing.setTransactionType(updatedDTO.getTransactionType());
    }
    if (updatedDTO.getDescription() != null) {
      existing.setDescription(updatedDTO.getDescription());
    }
    if (updatedDTO.getAccountId() != null) {
      var newAccount = accountRepository.findById(updatedDTO.getAccountId())
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