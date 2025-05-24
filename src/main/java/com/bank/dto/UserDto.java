package com.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;


/**
 * Represents a user entity in the banking system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long id;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private String passwordHash;
  private LocalDateTime createdAt = LocalDateTime.now();

  private Set<AccountDto> accounts;

}