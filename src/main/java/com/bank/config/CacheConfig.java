package com.bank.config;

import com.bank.utils.InMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up in-memory caches used throughout the application.
 * Provides bean definitions for various caches to store frequently accessed data.
 */
@Configuration
public class CacheConfig {

  /**
   * Creates a cache for storing account entities.
   *
   * @return new instance of InMemoryCache configured for Account objects
   */
  @Bean
  public InMemoryCache<Long, Object> accountCache() {
    return new InMemoryCache<>();
  }

  /**
   * Creates a cache for storing card entities.
   *
   * @return new instance of InMemoryCache configured for Card objects
   */
  @Bean
  public InMemoryCache<Long, Object> cardCache() {
    return new InMemoryCache<>();
  }

  /**
   * Creates a cache for storing user entities.
   *
   * @return new instance of InMemoryCache configured for User objects
   */
  @Bean
  public InMemoryCache<Long, Object> userCache() {
    return new InMemoryCache<>();
  }
}