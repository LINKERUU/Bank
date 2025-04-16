package com.bank.serviceImpl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

  /**
   * Verifies if a plain text password matches a hashed password.
   *
   * @param plainPassword  the plain text password to check
   * @param hashedPassword the hashed password to compare against
   * @return true if the passwords match, otherwise false
   */
  public boolean checkPassword(String plainPassword, String hashedPassword) {
    return passwordEncoder.matches(plainPassword, hashedPassword);
  }
}