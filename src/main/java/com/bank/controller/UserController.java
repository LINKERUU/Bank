package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.User;
import com.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing user-related operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final String USER_NOT_FOUND_MESSAGE = "Not found User with id: ";

  private final UserService userService;

  /**
   * Constructs a new UserController with the specified UserService.
   *
   * @param userService the service for managing users
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Retrieves all users.
   *
   * @return a list of all users
   */
  @Operation(summary = "Получить всех пользователей",
          description = "Возвращает список всех пользователей")
  @ApiResponse(responseCode = "200", description = "Пользователи успешно получены")
  @GetMapping
  public List<User> findAllUsers() {
    return userService.findAllUsers();
  }

  /**
   * Retrieves a user by ID.
   *
   * @param id the ID of the user to retrieve
   * @return the user with the specified ID
   * @throws ResourceNotFoundException if the user is not found
   */
  @Operation(summary = "Получить пользователя по ID",
          description = "Возвращает пользователя по его идентификатору")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Пользователь найден"),
      @ApiResponse(responseCode = "404", description = "Пользователь не найден")
  })
  @GetMapping("/{id}")
  public User findUserById(@PathVariable Long id) {
    return userService.findUserById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));
  }

  /**
   * Creates a new user.
   *
   * @param user the user to create
   * @return the created user
   */
  @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя")
  @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
    try {
      User createdUser = userService.createUser(user);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid user data: " + e.getMessage());
    }
  }

  /**
   * Creates multiple users in a batch.
   *
   * @param users the list of users to create
   * @return the list of created users
   */
  @Operation(summary = "Массовое создание пользователей",
          description = "Создает несколько пользователей одновременно")
  @ApiResponse(responseCode = "201", description = "Пользователи успешно созданы")
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<User> createUsers(@RequestBody List<User> users) {
    return userService.createUsers(users);
  }

  /**
   * Updates an existing user.
   *
   * @param id the ID of the user to update
   * @param user the updated user details
   * @return the updated user
   * @throws ResourceNotFoundException if the user is not found
   */
  @Operation(summary = "Обновить пользователя",
          description = "Обновляет существующего пользователя")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
      @ApiResponse(responseCode = "404", description = "Пользователь не найден")
  })
  @PutMapping("/{id}")
  public User updateUser(@PathVariable Long id, @RequestBody User user) {
    if (userService.findUserById(id).isEmpty()) {
      throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
    }
    return userService.updateUser(id, user);
  }

  /**
   * Deletes a user by ID.
   *
   * @param id the ID of the user to delete
   * @throws ResourceNotFoundException if the user is not found
   */
  @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
      @ApiResponse(responseCode = "404", description = "Пользователь не найден")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long id) {
    if (userService.findUserById(id).isEmpty()) {
      throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
    }
    userService.deleteUser(id);
  }
}