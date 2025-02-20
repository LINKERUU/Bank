package com.bank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a bank account with an ID, owner name, and balance.
 */
@Builder
@Getter
@AllArgsConstructor
public class Account {

  /**
   * Unique account identifier.
   */
  private int accountId;

  /**
   * Name of the account owner.
   */
  private String ownerName;

  /**
   * Account balance.
   */
  private double balance;
}