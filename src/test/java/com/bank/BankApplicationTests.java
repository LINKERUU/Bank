package com.bank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
@ActiveProfiles("test")  // Важно: использует профиль "test"
class BankApplicationTests {
    @Test
    void contextLoads() {
        // Тест проверяет только загрузку контекста
    }
}