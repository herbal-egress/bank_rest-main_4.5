package com.example.bankcards;

// Добавленный код: Импорт аннотации для запуска Spring Boot приложения.
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Добавленный код: Аннотация для запуска Spring Boot
public class BankRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args); // Добавленный код: Точка входа
    }
}