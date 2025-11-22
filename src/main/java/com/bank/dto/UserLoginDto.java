package com.bank.dto;


import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

  @NotNull(message = "Login cannot be null")
  private String login;

  @NotNull(message = "Password cannot be null")
  private String password;
}