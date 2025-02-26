package com.bank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bank account with an ID, owner name, and balance.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Account {
  private int id;
  private String ownerName;
  private double balance;
}