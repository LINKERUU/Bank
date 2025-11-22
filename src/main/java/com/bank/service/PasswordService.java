package com.bank.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service for handling password-related operations, such as hashing and verification.
 */
@Service
public class PasswordService {

  private final BCryptPasswordEncoder passwordEncoder;

  /**
   * Constructs a new PasswordService with a default BCryptPasswordEncoder.
   */
  public PasswordService() {
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  /**
   * Hashes a plain text password using BCrypt.
   *
   * @param plainPassword the plain text password to hash
   * @return the hashed password
   */
  public String hashPassword(String plainPassword) {
    return passwordEncoder.encode(plainPassword);
  }

  public  boolean matchPassword(String plainPassword, String hashedPassword) {
    return passwordEncoder.matches(plainPassword, hashedPassword);
  }

}