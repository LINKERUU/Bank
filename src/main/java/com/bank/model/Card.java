package com.bank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
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
import jakarta.validation.constraints.Size;
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

  @NotNull(message = "Card number is required")
  @Size(min = 16, max = 16, message = "Card number must be exactly 16 digits")
  @Pattern(regexp = "^\\d+$", message = "Card number must contain only digits")
  @Column(name = "card_number", unique = true)
  private String cardNumber;

  @NotNull(message = "Expiration date is required")
  @Future(message = "Expiration date must be in the future")
  @Column(name = "expiration_date")
  private YearMonth expirationDate;

  @NotNull(message = "CVV code is required")
  @Size(min = 3, max = 3, message = "CVV code must be exactly 3 digits")
  @Pattern(regexp = "^\\d+$", message = "CVV code must contain only digits")
  @Column(name = "cvv")
  private String cvv;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  @NotNull(message = "Card must be linked to an account")
  private Account account;
}