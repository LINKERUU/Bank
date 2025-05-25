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
  public static void main(String[] args) {
    try {
      SpringApplication.run(BankApplication.class, args);
      System.out.println("Application started successfully!");
    } catch (Exception e) {
      System.err.println("Application failed to start:");
      e.printStackTrace();
      System.exit(1);
    }
  }

}
