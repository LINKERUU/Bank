package com.bank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Data Transfer Object representing a bank card.
 * Contains card information including number, expiration date, and associated account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDto {
  private Long id;
  private String cardNumber;
  private YearMonth expirationDate;
  private String cvv;
  private Long accountId;
  private String accountNumber; // Added for display convenience
}