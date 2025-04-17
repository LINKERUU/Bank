package com.bank.controller;

import com.bank.model.Account;
import com.bank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Account Controller", description = "API для работы со счетами")
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
  @Operation(summary = "Получить все счета",
          description = "Возвращает список всех банковских счетов")
  @ApiResponse(responseCode = "200", description = "Счета успешно получены")
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
  @Operation(summary = "Получить счет по ID", description = "Возвращает счет по его идентификатору")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счет найден"),
      @ApiResponse(responseCode = "404", description = "Счет не найден")
  })
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
  @Operation(summary = "Создать новый счет", description = "Создает новый банковский счет")
  @ApiResponse(responseCode = "201", description = "Счет успешно создан")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
    Account createdAccount = accountService.createAccount(account);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
  }

  /**
   * Creates multiple accounts in a batch.
   *
   * @param accounts the list of accounts to create
   * @return the list of created accounts
   */
  @Operation(summary = "Массовое создание счетов",
          description = "Создает несколько счетов одновременно")
  @ApiResponse(responseCode = "201", description = "Счета успешно созданы")
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
  @Operation(summary = "Обновить счет", description = "Обновляет существующий счет")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счет успешно обновлен"),
      @ApiResponse(responseCode = "404", description = "Счет не найден")
  })
  @PutMapping("/{id}")
  public Account updateAccount(@PathVariable Long id, @RequestBody Account account) {
    return accountService.updateAccount(id, account);
  }

  /**
   * Creates a new account.
   * account the account to create
   * return the created account
   */
  @Operation(summary = "Массовое обновление счетов",
          description = "Обновляет несколько счетов одновременно")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счета успешно обновлены"),
      @ApiResponse(responseCode = "400", description = "Некорректные данные"),
      @ApiResponse(responseCode = "404", description = "Один из счетов не найден")
  })
  @PutMapping("/batch")
  public ResponseEntity<List<Account>> updateAccounts(@Valid @RequestBody List<Account> accounts) {
    return ResponseEntity.ok(accountService.updateAccounts(accounts));
  }

  /**
   * Deletes an account by its ID.
   *
   * @param id the ID of the account to delete
   */
  @Operation(summary = "Удалить счет", description = "Удаляет счет по его ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Счет успешно удален"),
      @ApiResponse(responseCode = "404", description = "Счет не найден")
  })
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
  @Operation(summary = "Получить счета по email пользователя",
          description = "Возвращает счета, связанные с email пользователя")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счета найдены"),
      @ApiResponse(responseCode = "404", description = "Счета не найдены")
  })
  @GetMapping("/by-email")
  public ResponseEntity<List<Account>> getAccountsByUserEmail(@RequestParam String email) {
    return ResponseEntity.ok(accountService.findByUserEmail(email));
  }

  @Operation(summary = "Получить счета с картами",
          description = "Возвращает счета, к которым привязаны карты")
  @ApiResponse(responseCode = "200", description = "Счета с картами найдены")
  @GetMapping("/with-cards")
  public ResponseEntity<List<Account>> getAccountsWithCards() {
    return ResponseEntity.ok(accountService.findAccountsWithCards());
  }

  /**
   * Deletes multiple accounts by their IDs.
   * param ids the list of account IDs to delete
   */
  @Operation(summary = "Массовое удаление счетов",
          description = "Удаляет несколько счетов по их ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Счета успешно удалены"),
      @ApiResponse(responseCode = "404", description = "Один из счетов не найден")
  })
  @DeleteMapping("/batch")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccounts(@RequestBody List<Long> ids) {
    accountService.deleteAccounts(ids);
  }

}