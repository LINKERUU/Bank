package com.bank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for bank transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {
  private Long id;
  private Double amount;
  private String transactionType;
  private String description;
  private LocalDateTime transactionDate;
  private Long accountId;
  private String accountNumber; // Добавлено для удобства отображения
}
