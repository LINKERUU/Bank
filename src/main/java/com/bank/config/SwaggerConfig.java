package com.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * Defines API metadata and server configuration for the Bank Service.
 */
@Configuration
public class SwaggerConfig {

  /**
   * Creates and configures the OpenAPI documentation bean.
   *
   * @return OpenAPI object containing API documentation details including:
   *         - Server URL (localhost:8080)
   *         - API title, version and description
   */
  @Bean
  public OpenAPI api() {
    return new OpenAPI()
           .servers(List.of(new Server().url("http://localhost:8080"))
           )
           .info(new Info()
                   .title("Bank Service API")
                   .version("1.0")
                   .description("API для банковских операций")
           );
  }
}
