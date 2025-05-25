package com.bank.service;

import com.bank.dto.UserDto;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 */
public interface UserService {

  /**
   * Получает список всех пользователей.
   *
   * @return список пользователей
   */
  List<UserDto> findAllUsers();

  /**
   * Находит пользователя по идентификатору.
   *
   * @param id идентификатор пользователя
   * @return Optional с данными пользователя
   */
  Optional<UserDto> findUserById(Long id);

  /**
   * Создает нового пользователя.
   *
   * @param userDto данные нового пользователя
   * @return созданный пользователь
   */
  UserDto createUser(UserDto userDto);

  /**
   * Массово создает пользователей.
   *
   * @param usersDto список пользователей для создания
   * @return список созданных пользователей
   */
  List<UserDto> createUsers(List<UserDto> usersDto);

  /**
   * Обновляет данные пользователя.
   *
   * @param id идентификатор пользователя
   * @param userDto новые данные пользователя
   * @return обновленные данные пользователя
   */
  UserDto updateUser(Long id, UserDto userDto);

  /**
   * Удаляет пользователя.
   *
   * @param id идентификатор пользователя
   */
  void deleteUser(Long id);
}