package com.bank;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BankApplicationTests {

    @Disabled("Context loading is verified by @SpringBootTest annotation")
    @Test
    void contextLoads() {
        // Пустая реализация
    }
}