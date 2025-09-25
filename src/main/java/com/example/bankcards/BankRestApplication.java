package com.example.bankcards;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication 
@Slf4j 
public class BankRestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args); 
        log.info("Приложение успешно запущено"); 
    }
}