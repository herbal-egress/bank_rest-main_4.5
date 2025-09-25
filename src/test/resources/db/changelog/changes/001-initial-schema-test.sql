-- добавленный код: SQL-скрипт для создания начальной схемы базы данных в схеме 'test' (дублирует 001-initial-schema.sql из public)
-- Все таблицы создаются с префиксом 'test.' для изоляции от public

-- Создание схемы test, если не существует
CREATE SCHEMA IF NOT EXISTS test;

-- изменил ИИ: удалил CREATE EXTENSION IF NOT EXISTS pgcrypto, т.к. тестовые данные не используют шифрование (encrypted_card_number - строка); это предотвращает сбой скрипта из-за прав суперпользователя

-- Создание таблицы roles в схеме test
CREATE TABLE IF NOT EXISTS test.roles (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(50) UNIQUE NOT NULL
);

-- Создание таблицы users в схеме test
CREATE TABLE IF NOT EXISTS test.users (
                                          id BIGSERIAL PRIMARY KEY,
                                          username VARCHAR(50) UNIQUE NOT NULL,
                                          password VARCHAR(255) NOT NULL
);

-- Создание таблицы users_roles для связи пользователей и ролей в схеме test
CREATE TABLE IF NOT EXISTS test.users_roles (
                                                user_id BIGINT NOT NULL REFERENCES test.users(id) ON DELETE CASCADE,
                                                role_id INTEGER NOT NULL REFERENCES test.roles(id) ON DELETE CASCADE,
                                                PRIMARY KEY (user_id, role_id)
);

-- Создание таблицы cards в схеме test
CREATE TABLE IF NOT EXISTS test.cards (
                                          id BIGSERIAL PRIMARY KEY,
                                          encrypted_card_number TEXT UNIQUE NOT NULL,
                                          owner_name VARCHAR(50) NOT NULL,
                                          expiration_date DATE NOT NULL,
                                          status VARCHAR(20) NOT NULL,
                                          balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                                          user_id BIGINT NOT NULL REFERENCES test.users(id)
);

-- Создание таблицы transactions в схеме test
CREATE TABLE IF NOT EXISTS test.transactions (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 from_card_id BIGINT NOT NULL REFERENCES test.cards(id) ON DELETE CASCADE,
                                                 to_card_id BIGINT NOT NULL REFERENCES test.cards(id) ON DELETE CASCADE,
                                                 amount DOUBLE PRECISION NOT NULL,
                                                 timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 status VARCHAR(20) NOT NULL
);

-- добавленный код: Заполнение таблицы ролей в схеме test (дублирует public)
-- изменил ИИ: удалил ON CONFLICT (name) DO NOTHING, чтобы скрипт сбоил при duplicate (для диагностики, если схема не очищена)
INSERT INTO test.roles (name) VALUES ('USER');
INSERT INTO test.roles (name) VALUES ('ADMIN');

-- добавленный код: Создание тестовых пользователей с BCrypt-паролями в схеме test (дублирует public)
-- Юзер 'user' с паролем 'user' (BCrypt-хеш)
-- изменил ИИ: удалил ON CONFLICT (username) DO NOTHING
INSERT INTO test.users (username, password)
VALUES ('user', '$2a$12$.e.FugbBEPzCDxLsZEA5BeRpD.gfvnrMB3CiqwGfDKY7HuqJDfjUG');

-- Админ 'admin' с паролем 'admin' (BCrypt-хеш)
-- изменил ИИ: удалил ON CONFLICT (username) DO NOTHING
INSERT INTO test.users (username, password)
VALUES ('admin', '$2a$12$KTh8bU.CtA/7eHQum36wo.SwaTgs6n.c1s26qReAabmsF4YN5cbMy');

-- добавленный код: Назначение ролей тестовым пользователям в схеме test (дублирует public)
-- Назначаем роль USER пользователю 'user'
-- изменил ИИ: удалил ON CONFLICT (user_id, role_id) DO NOTHING
INSERT INTO test.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM test.users u, test.roles r
WHERE u.username = 'user' AND r.name = 'USER';

-- Назначаем роль ADMIN пользователю 'admin'
-- изменил ИИ: удалил ON CONFLICT (user_id, role_id) DO NOTHING
INSERT INTO test.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM test.users u, test.roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';