-- добавленный код: SQL-скрипт для создания начальной схемы базы данных
-- Создание расширения pgcrypto для шифрования номера карты
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Создание таблицы roles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Создание таблицы users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Создание таблицы users_roles для связи пользователей и ролей
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Создание таблицы cards
CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    encrypted_card_number TEXT UNIQUE NOT NULL,
    owner_name VARCHAR(50) NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    user_id BIGINT NOT NULL REFERENCES users(id)
);

-- Создание таблицы transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    to_card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL
);

-- добавленный код: Заполнение таблицы ролей
INSERT INTO roles (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;

-- добавленный код: Создание тестовых пользователей с BCrypt-паролями
-- Юзер 'user' с паролем 'user' (BCrypt-хеш)
INSERT INTO users (username, password)
VALUES ('user', '$2a$12$.e.FugbBEPzCDxLsZEA5BeRpD.gfvnrMB3CiqwGfDKY7HuqJDfjUG')
ON CONFLICT (username) DO NOTHING;

-- Админ 'admin' с паролем 'admin' (BCrypt-хеш)
INSERT INTO users (username, password)
VALUES ('admin', '$2a$12$KTh8bU.CtA/7eHQum36wo.SwaTgs6n.c1s26qReAabmsF4YN5cbMy')
ON CONFLICT (username) DO NOTHING;

-- добавленный код: Назначение ролей тестовым пользователям
-- Назначаем роль USER пользователю 'user'
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Назначаем роль ADMIN пользователю 'admin'
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- добавленный код: Отладочная информация о созданных пользователях и ролях
DO $$
BEGIN
    RAISE NOTICE 'Создано пользователей: %', (SELECT COUNT(*) FROM users);
    RAISE NOTICE 'Назначено ролей: %', (SELECT COUNT(*) FROM users_roles);
END $$;