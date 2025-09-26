package com.example.bankcards.service;

import com.example.bankcards.service.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

// добавленный код: Простой тест для EncryptionService, так как он не зависит от базы данных
@SpringBootTest
class EncryptionServiceTest {

    @Autowired
    private EncryptionServiceImpl encryptionService;

    // добавленный код: Тест шифрования данных
    @Test
    void encrypt_Success() {
        String data = "testData";
        String result = encryptionService.encrypt(data);
        assertEquals("encrypted_testData", result);
    }
}