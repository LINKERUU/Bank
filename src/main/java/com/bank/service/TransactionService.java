// TransactionService.java
package com.bank.service;

import com.bank.dto.TransactionDto;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

  List<TransactionDto> findAllTransactions();

  Optional<TransactionDto> findTransactionById(Long id);

  TransactionDto createTransaction(TransactionDto transactionDTO);

  TransactionDto updateTransaction(Long id, TransactionDto transactionDTO);

  void deleteTransaction(Long id);
}