package com.bank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a bank card entity.
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Card number cannot be null")
  @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
  private String cardNumber;

  @Future(message = "Expiration date must be in the future")
  @NotNull(message = "Expiration date cannot be null")
  private YearMonth expirationDate;

  @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
  @NotNull(message = "CVV cannot be null")
  private String cvv;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  @NotNull(message = "Card must be linked to an account")
  private Account account;


}