package com.example.bankcards.service;
import com.example.bankcards.service.impl.EncryptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
class EncryptionServiceTest {
    @Autowired
    private EncryptionServiceImpl encryptionService;
    @Test
    void encrypt_Success() {
        String data = "testData";
        String result = encryptionService.encrypt(data);
        assertEquals("encrypted_testData", result);
    }
}