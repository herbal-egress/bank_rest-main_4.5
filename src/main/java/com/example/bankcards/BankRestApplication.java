package com.example.bankcards;

// Добавленный код: Импорт аннотации для запуска Spring Boot приложения.
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication // Добавленный код: Аннотация для запуска Spring Boot
@Slf4j // добавленный код: Добавляет private static final Logger log = LoggerFactory.getLogger(BankRestApplication.class); для логирования в main классе.
public class BankRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args); // Добавленный код: Точка входа
        log.info("Приложение успешно запущено"); // Логирует успешный запуск приложения через SLF4J.
    }
}