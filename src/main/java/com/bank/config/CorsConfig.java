package com.bank.config;  // Замените на ваш реальный пакет

import com.bank.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Deletes a card by ID.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/**")
              .allowedOrigins("https://frontend-for-bank-production.up.railway.app:3000")
              .allowedMethods("*")
              .allowedHeaders("*");
    }
}