package com.bank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuration class for setting up in-memory caches used throughout the application.
 * Provides bean definitions for various caches to store frequently accessed data.
 */
@RestController
public class TestController {
  /**
   * Configuration class for setting up in-memory caches used throughout the application.
   * Provides bean definitions for various caches to store frequently accessed data.
   */
  @GetMapping("/")
  public String hello() {
    return "Backend is up!";
  }
}
