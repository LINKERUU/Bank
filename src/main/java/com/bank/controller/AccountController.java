package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.AccountService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller for managing bank accounts.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  /**
   * Constructor.
   */

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  /**
   * Retrieves all accounts.
   *
   * @return a list of all accounts
   */
  @GetMapping
  public List<Account> findAllAccounts() {
    return accountService.findAllAccounts();
  }

  /**
   * Retrieves an account by its ID.
   *
   * @param id the ID of the account to retrieve
   * @return the account with the specified ID, if found
   */
  @GetMapping("/{id}")
  public Optional<Account> findAccountById(@PathVariable Long id) {
    return accountService.findAccountById(id);
  }

  /**
   * Creates a new account.
   *
   * @param account the account to create
   * @return the created account
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Account createAccount(@RequestBody Account account) {
    return accountService.createAccount(account);
  }

  /**
   * Creates multiple accounts in a batch.
   *
   * @param accounts the list of accounts to create
   * @return the list of created accounts
   */
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<Account> createAccounts(@RequestBody List<Account> accounts) {
    return accountService.createAccounts(accounts);
  }

  /**
   * Updates an existing account.
   *
   * @param id      the ID of the account to update
   * @param account the updated account details
   * @return the updated account
   */
  @PutMapping("/{id}")
  public Account updateAccount(@PathVariable Long id, @RequestBody Account account) {
    return accountService.updateAccount(id, account);
  }

  /**
   * Deletes an account by its ID.
   *
   * @param id the ID of the account to delete
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@PathVariable Long id) {
    accountService.deleteAccount(id);
  }

  /**
   * Retrieves accounts associated with a specific user email.
   *
   * @param email the email of the user to filter accounts by
   * @return a list of accounts associated with the specified email
   */
  @GetMapping("/filterByUserEmail")
  public ResponseEntity<List<Account>> getAccountsByUserEmail(@RequestParam String email) {
    List<Account> accounts = accountService.findByUserEmail(email);
    return ResponseEntity.ok(accounts);
  }

}