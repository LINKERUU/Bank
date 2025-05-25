package com.bank.controller;

import com.bank.dto.UserDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API для работы с пользователями")
public class UserController {

  private static final String USER_NOT_FOUND_MESSAGE = "Not found User with id: ";

  private final UserService userService;

  /**
   * Конструктор контроллера.
   *
   * @param userService сервис пользователей
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Получает список всех пользователей.
   *
   * @return список пользователей
   */
  @Operation(summary = "Получить всех пользователей",
          description = "Возвращает список всех пользователей")
  @ApiResponse(responseCode = "200", description = "Пользователи успешно получены")
  @GetMapping
  public List<UserDto> findAllUsers() {
    return userService.findAllUsers();
  }

  /**
   * Получает пользователя по идентификатору.
   *
   * @param id идентификатор пользователя
   * @return данные пользователя
   * @throws ResourceNotFoundException если пользователь не найден
   */
  @Operation(summary = "Получить пользователя по ID",
          description = "Возвращает пользователя по его идентификатору")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Пользователь найден"),
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
  })
  @GetMapping("/{id}")
  public UserDto findUserById(@PathVariable Long id) {
    return userService.findUserById(id)
            .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id));
  }

  /**
   * Создает нового пользователя.
   *
   * @param userDto данные нового пользователя
   * @return созданный пользователь
   * @throws IllegalArgumentException если данные пользователя неверные
   */
  @Operation(summary = "Создать нового пользователя",
          description = "Создает нового пользователя")
  @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
    try {
      UserDto createdUser = userService.createUser(userDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid user data: " + e.getMessage());
    }
  }

  /**
   * Массово создает пользователей.
   *
   * @param usersDto список пользователей для создания
   * @return список созданных пользователей
   */
  @Operation(summary = "Массовое создание пользователей",
          description = "Создает несколько пользователей одновременно")
  @ApiResponse(responseCode = "201", description = "Пользователи успешно созданы")
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<UserDto> createUsers(@RequestBody List<UserDto> usersDto) {
    return userService.createUsers(usersDto);
  }

  /**
   * Обновляет данные пользователя.
   *
   * @param id идентификатор пользователя
   * @param userDto новые данные пользователя
   * @return обновленные данные пользователя
   * @throws ResourceNotFoundException если пользователь не найден
   */
  @Operation(summary = "Обновить пользователя",
          description = "Обновляет существующего пользователя")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен"),
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
  })
  @PutMapping("/{id}")
  public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
    if (userService.findUserById(id).isEmpty()) {
      throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + id);
    }
    return userService.updateUser(id, userDto);
  }

  /**
   * Удаляет пользователя.
   *
   * @param id идентификатор пользователя
   * @throws ResourceNotFoundException если пользователь не найден
   */
  @Operation(summary = "Удалить пользователя",
          description = "Удаляет пользователя по его ID")
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