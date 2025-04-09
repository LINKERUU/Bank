package com.bank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
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

  @NotNull(message = "Номер карты обязателен")
  @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 цифр")
  @Pattern(regexp = "^[0-9]+$", message = "Номер карты должен содержать только цифры")
  @Column(name = "card_number", unique = true)
  private String cardNumber;

  @NotNull(message = "Срок действия обязателен")
  @Future(message = "Срок действия карты должен быть в будущем")
  @Column(name = "expiration_date")
  private YearMonth expirationDate;

  @NotNull(message = "CVV код обязателен")
  @Size(min = 3, max = 3, message = "CVV код должен содержать 3 цифры")
  @Pattern(regexp = "^[0-9]+$", message = "CVV код должен содержать только цифры")
  @Column(name = "cvv")
  private String cvv;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  @NotNull(message = "Карта должна быть привязана к счету")
  private Account account;
}