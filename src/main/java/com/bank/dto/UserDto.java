package com.bank.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



/**
 * Represents a user entity in the banking system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long id;

  private String login;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private String passwordHash;
  private LocalDateTime createdAt = LocalDateTime.now();

  private Set<AccountDto> accounts;

}