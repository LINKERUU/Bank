package com.bank.config;

import org.springframework.beans.factory.annotation.Value; // Импортируйте @Value
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for setting up in-memory caches used throughout the application.
 * Provides bean definitions for various caches to store frequently accessed data.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            // Используем инжектированное значение
            .allowedOrigins("https://frontend-for-bank-production.up.railway.app")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("Content-Type", "Authorization")
            .allowCredentials(true);
  }
}