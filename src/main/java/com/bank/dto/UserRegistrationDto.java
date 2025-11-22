package com.bank.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

  @NotNull(message = "Login cannot be null")
  private String login;

  @NotNull(message = "firstName cannot be null")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  private String firstName;

  @NotNull(message = "lastName cannot be null")
  @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
  private String lastName;

  @NotNull(message = "email cannot be null")
  @Email(message = "email should be valid")
  private String email;

  @NotNull(message = "phone cannot be null")
  private String phone;

  @NotNull(message = "password cannot be null")
  @Size(min = 8,message = "password should be at least 8 characters")
  private String password;
}
