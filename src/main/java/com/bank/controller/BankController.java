package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.BankService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 * Controller for handling bank account operations.
 */
@RestController
@RequestMapping("api/accounts")
public final class BankController {

  /**
   * Service for handling bank account logic.
   */
  private final BankService bankService;

  /**
   * Constructor for BankController.
   *
   * @param service service for handling bank accounts
   */
  public BankController(final BankService service) {
    this.bankService = service;
  }

  /**
   * Retrieve a list of all accounts.
   *
   * @return list of accounts
   */
  @GetMapping
  public List<Account> findAllAccounts() {
    return bankService.findAllAccounts();
  }

  /**
   * Retrieve an account by owner name.
   *
   * @param ownerName owner's name
   * @return account of the owner
   */
  @GetMapping("/by-owner")
  public Account getAccountByOwnerName(@RequestParam final String ownerName) {
    return bankService.getAccountByOwnerName(ownerName);
  }

  /**
   * Retrieve an account by ID.
   *
   * @param accountId account identifier
   * @return account
   */
  @GetMapping("/{accountId}")
  public Account getAccountById(@PathVariable final int accountId) {
    return bankService.getAccountById(accountId);
  }
}