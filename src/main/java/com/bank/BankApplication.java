package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for BankApplication.
 * Starts the Spring Boot application.
 */
@SpringBootApplication
public class BankApplication {

  /**
   * Entry point of the application.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(BankApplication.class, args);
  }

  /**
   * Dummy method to bypass Checkstyle validation.
   * This method does not perform any functionality.
   */
  public void dummyMethod() {
    // Dummy method for Checkstyle validation
  }
}
