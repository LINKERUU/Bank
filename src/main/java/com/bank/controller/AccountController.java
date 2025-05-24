package com.bank.controller;

import com.bank.dto.AccountDto;
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
 * Контроллер для управления банковскими счетами.
 * Предоставляет API для создания, чтения, обновления и удаления счетов.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Controller", description = "API для работы со счетами")
public class AccountController {

  private final AccountService accountService;

  /**
   * Конструктор контроллера счетов.
   *
   * @param accountService сервис для работы со счетами
   */
  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  /**
   * Получает список всех банковских счетов.
   *
   * @return список всех счетов
   */
  @Operation(summary = "Получить все счета",
          description = "Возвращает список всех банковских счетов")
  @ApiResponse(responseCode = "200", description = "Счета успешно получены")
  @GetMapping
  public List<AccountDto> findAllAccounts() {
    return accountService.findAllAccounts();
  }

  /**
   * Получает счет по его идентификатору.
   *
   * @param id идентификатор счета
   * @return Optional с найденным счетом
   */
  @Operation(summary = "Получить счет по ID", description = "Возвращает счет по его идентификатору")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Счет найден"),
    @ApiResponse(responseCode = "404", description = "Счет не найден")
  })
  @GetMapping("/{id}")
  public Optional<AccountDto> findAccountById(@PathVariable Long id) {
    return accountService.findAccountById(id);
  }

  /**
   * Создает новый банковский счет.
   *
   * @param accountDto данные для создания счета
   * @return созданный счет
   */
  @Operation(summary = "Создать новый счет", description = "Создает новый банковский счет")
  @ApiResponse(responseCode = "201", description = "Счет успешно создан")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
    AccountDto createdAccount = accountService.createAccount(accountDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
  }

  /**
   * Создает несколько счетов одновременно.
   *
   * @param accountsDto список счетов для создания
   * @return список созданных счетов
   */
  @Operation(summary = "Массовое создание счетов",
          description = "Создает несколько счетов одновременно")
  @ApiResponse(responseCode = "201", description = "Счета успешно созданы")
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<AccountDto> createAccounts(@RequestBody List<AccountDto> accountsDto) {
    return accountService.createAccounts(accountsDto);
  }

  /**
   * Обновляет существующий счет.
   *
   * @param id идентификатор счета
   * @param accountDto новые данные счета
   * @return обновленный счет
   */
  @Operation(summary = "Обновить счет", description = "Обновляет существующий счет")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счет успешно обновлен"),
      @ApiResponse(responseCode = "404", description = "Счет не найден")
  })
  @PutMapping("/{id}")
  public AccountDto updateAccount(@PathVariable Long id, @RequestBody AccountDto accountDto) {
    return accountService.updateAccount(id, accountDto);
  }

  /**
   * Удаляет счет по его идентификатору.
   *
   * @param id идентификатор счета для удаления
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
   * Получает счета по email пользователя.
   *
   * @param email email пользователя
   * @return список счетов пользователя
   */
  @Operation(summary = "Получить счета по email пользователя",
          description = "Возвращает счета, связанные с email пользователя")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Счета найдены"),
    @ApiResponse(responseCode = "404", description = "Счета не найдены")
  })
  @GetMapping("/by-email")
  public ResponseEntity<List<AccountDto>> getAccountsByUserEmail(@RequestParam String email) {
    return ResponseEntity.ok(accountService.findByUserEmail(email));
  }

  /**
   * Получает счета, к которым привязаны карты.
   *
   * @return список счетов с картами
   */
  @Operation(summary = "Получить счета с картами",
          description = "Возвращает счета, к которым привязаны карты")
  @ApiResponse(responseCode = "200", description = "Счета с картами найдены")
  @GetMapping("/with-cards")
  public ResponseEntity<List<AccountDto>> getAccountsWithCards() {
    return ResponseEntity.ok(accountService.findAccountsWithCards());
  }
}