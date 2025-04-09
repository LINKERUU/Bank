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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a transaction entity in the banking system.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Amount cannot be null")
  @Positive(message = "Amount must be positive")
  @Column(name = "amount")
  private Double amount;

  @NotNull(message = "Transaction type cannot be null")
  @Pattern(regexp = "credit|debit", flags = Pattern.Flag.CASE_INSENSITIVE,
          message = "Transaction type must be 'credit' or 'debit'")
  @Column(name = "transaction_type")
  private String transactionType;

  @Column(name = "description")
  private String description;

  @Column(name = "transaction_date", nullable = false)
  private LocalDateTime transactionDate = LocalDateTime.now();

  @JsonBackReference
  @ManyToOne(
          fetch = FetchType.LAZY,
          cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Transient
  private Long accountId;
}