-- добавленный код: SQL-скрипт для заполнения таблицы cards тестовыми данными в схеме 'test' (дублирует 002-initial-data.sql из public)
-- Все вставки с префиксом 'test.cards' и ссылки на test.users (user_id=1 для user, 2 для admin)

-- Карта 1 для user (Visa-like)
-- изменил ИИ: удалил ON CONFLICT (encrypted_card_number) DO NOTHING, чтобы скрипт сбоил при duplicate
INSERT INTO test.cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES (
           'encrypted_1231111111111111',
           'Ivan Ivanov',
           '2026-12-31'::DATE,
           'ACTIVE',
           1000.0,
           1  -- Ссылка на test.users.id=1
       );

-- Карта 2 для user (Mastercard-like)
-- изменил ИИ: удалил ON CONFLICT
INSERT INTO test.cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES (
           'encrypted_2345555555554444',
           'Anna Petrova',
           '2025-06-30'::DATE,
           'ACTIVE',
           2000.0,
           1
       );

-- Карта 3 для user (Amex-like)
-- изменил ИИ: удалил ON CONFLICT
INSERT INTO test.cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES (
           'encrypted_3452822463100057',
           'Mikhail Sidorov',
           '2027-03-31'::DATE,
           'ACTIVE',
           1500.0,
           1
       );

-- Карта 1 для admin (Visa-like)
-- изменил ИИ: удалил ON CONFLICT
INSERT INTO test.cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES (
           'encrypted_4561111111112222',
           'Dmitry Kuznetsov',
           '2026-09-30'::DATE,
           'ACTIVE',
           5000.0,
           2
       );

-- Карта 2 для admin (Mastercard-like)
-- изменил ИИ: удалил ON CONFLICT
INSERT INTO test.cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES (
           'encrypted_5675555555553333',
           'Elena Smirnova',
           '2025-12-31'::DATE,
           'ACTIVE',
           3000.0,
           2
       );