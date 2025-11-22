package com.bank.service.impl;

import com.bank.dto.*;
import com.bank.exception.UnauthorizedException;
import com.bank.model.User;
import com.bank.repository.UserRepository;
import com.bank.service.AuthService;
import com.bank.service.PasswordService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {


  private final PasswordService passwordService;
  private final UserRepository userRepository;

  @Override
  public AuthResponseDto login(UserLoginDto loginDto) {
    Optional<User> user = userRepository.findByLogin(loginDto.getLogin());

    if(!(passwordService.matchPassword(loginDto.getPassword(),user.get().getPasswordHash()))) {
      throw new UnauthorizedException("Invalid email or password");
    }

    UserDto userDto = convertToDto(user.get());

    return  new AuthResponseDto("Login successful",userDto);

  }



  @Override
  public AuthResponseDto register(UserRegistrationDto registrationDto) {
    if (userRepository.findByLogin(registrationDto.getLogin()).isPresent()) {
      throw new UnauthorizedException("Login already exists");
    }

    User user = new User();
    user.setLogin(registrationDto.getLogin());
    user.setFirstName(registrationDto.getFirstName());
    user.setLastName(registrationDto.getLastName());
    user.setEmail(registrationDto.getEmail());
    user.setPhone(registrationDto.getPhone());

    user.setPasswordHash(passwordService.hashPassword(registrationDto.getPassword()));

    User savedUser = userRepository.save(user);

    UserDto userDto = convertToDto(savedUser);

    return new AuthResponseDto("Register successful",userDto);

  }

  @Override
  public AuthResponseDto forgotPassword(UserForgotDto forgotDto) {
    Optional<User> user = userRepository.findByLogin(forgotDto.getLogin());

    if (user.isEmpty()) {
      throw new UnauthorizedException("Cannot find login");
    }

    return new AuthResponseDto("Login exists", convertToDto(user.get()));
  }

  @Override
  public AuthResponseDto resetPassword(UserResetDto resetDto) {

    Optional<User> user = userRepository.findByLogin(resetDto.getLogin());

    if (user.isEmpty()) {
      throw new UnauthorizedException("Cannot find login");
    }
    user.get().setPasswordHash(passwordService.hashPassword(resetDto.getPassword()));
    userRepository.save(user.get());

    return new AuthResponseDto("Password updated successfully", convertToDto(user.get()));
  }



  @Override
  public UserDto convertToDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setLogin(user.getLogin());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setEmail(user.getEmail());
    userDto.setPhone(user.getPhone());
    userDto.setCreatedAt(user.getCreatedAt());
    return userDto;
  }

}
