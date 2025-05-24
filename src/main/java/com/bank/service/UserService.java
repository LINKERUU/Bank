package com.bank.service;

import com.bank.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

  List<UserDto> findAllUsers();

  Optional<UserDto> findUserById(Long id);

  UserDto createUser(UserDto userDTO);

  List<UserDto> createUsers(List<UserDto> usersDTO);

  UserDto updateUser(Long id, UserDto userDTO);

  void deleteUser(Long id);
}