package com.bank.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a bank account.
 * Contains account details including account number, balance,
 * associated cards, users and transactions.
 */

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Account number cannot be blank")
  @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
  @Pattern(regexp = "^\\d+$", message = "Account number must contain only digits")
  @Column(name = "account_number", unique = true, nullable = false, length = 20)
  private String accountNumber;

  @NotNull(message = "Balance cannot be null")
  @DecimalMin(value = "0.0", message = "Balance cannot be negative")
  @Column(name = "balance", nullable = false)
  private Double balance = 0.0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Card> cards;

  @ManyToMany(
          cascade = {CascadeType.MERGE, CascadeType.REFRESH},
          fetch = FetchType.LAZY)
  @JoinTable(
          name = "user_accounts",
          inverseJoinColumns = @JoinColumn(name = "user_id"),
          joinColumns = @JoinColumn(name = "account_id"))
  private Set<User> users;

  @JsonManagedReference
  @OneToMany(
          mappedBy = "account",
          cascade = CascadeType.ALL,
          fetch = FetchType.LAZY,
          orphanRemoval = true)
  private List<Transaction> transactions;
}