package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.AccountService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling bank account operations.
 */
@RestController
@RequestMapping("api/accounts")
public final class AccountController {


  private final AccountService accountService;

  /**
   * Constructor for BankController.
   *
   * @param service service for handling bank accounts
   */
  public AccountController(final AccountService service) {
    this.accountService = service;
  }

  /**
   * Retrieve a list of all accounts.
   *
   * @return list of accounts
   */
  @GetMapping
  public List<Account> findAllAccounts() {
    return accountService.findAllAccounts();
  }

  /**
   * Retrieve an account by owner name.
   *
   * @param ownerName owner's name
   * @return account of the owner
   */
  @GetMapping("/by-owner")
  public Account getAccountByOwnerName(@RequestParam final String ownerName) {
    Account account = accountService.getAccountByOwnerName(ownerName);
    if (account == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Account not found with owner name: " + ownerName);
    }
    return account;
  }

  /**
   * Retrieve an account by ID.
   *
   * @param id account identifier
   * @return account
   */
  @GetMapping("/{id}")
  public Account getAccountById(@PathVariable final int id) { // Изменено с accountId на id
    Account account = accountService.getAccountById(id); // Изменено с accountId на id
    if (account == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Account not found with ID: " + id); // Изменено с accountId на id
    }
    return account;
  }
}