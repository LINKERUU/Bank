package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
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
    // Для диагностики deadlock
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      System.err.println("Uncaught exception in thread: " + t.getName());
      e.printStackTrace();
    });
  }

  public static void main(String[] args) {
    new SpringApplicationBuilder(BankApplication.class)
            .listeners(new ApplicationPidFileWriter())
            .run(args);
  }
}
