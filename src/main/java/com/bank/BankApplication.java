package com.bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  static {
    System.setProperty("tomcat.util.compat.JreCompat.skipJre22Check", "true");
  }

  /**
   * Main class for BankApplication.
   * Starts the Spring Boot application.
   */
  public static void main(String[] args) {


    SpringApplication.run(BankApplication.class, args);
  }
}