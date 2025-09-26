package com.example.bankcards.service.impl;

import com.example.bankcards.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class EncryptionServiceImpl implements EncryptionService {

    @Override
    public String encrypt(String data) {
        log.debug("Шифрование данных: {}", data.substring(0, Math.min(4, data.length())) + "***");
        return "encrypted_" + data;
    }
}