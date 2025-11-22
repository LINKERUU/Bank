package com.bank.service;

import com.bank.dto.*;
import com.bank.model.User;
import jakarta.validation.Valid;

public interface AuthService {
  AuthResponseDto login(@Valid UserLoginDto loginDto);
  AuthResponseDto register(@Valid UserRegistrationDto loginDto);
  AuthResponseDto forgotPassword(@Valid UserForgotDto forgotDto);
  AuthResponseDto resetPassword(UserResetDto resetDto);
  UserDto convertToDto(User user);
}
