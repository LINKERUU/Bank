package com.bank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) для представления банковского счета.
 * Содержит информацию о счете, включая номер, баланс, дату создания,
 * привязанные карты, пользователей и транзакции.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDto {
  private Long id;
  private String accountNumber;
  private Double balance;
  private LocalDateTime createdAt;
  private Set<CardDto> cards;
  private Set<UserDto> users;
  private Set<TransactionDto> transactions;
}