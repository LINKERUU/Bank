package com.bank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

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