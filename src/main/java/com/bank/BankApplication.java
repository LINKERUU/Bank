package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class for BankApplication.
 * Starts the Spring Boot application.
 */
@EnableScheduling
@EnableAspectJAutoProxy
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

}
