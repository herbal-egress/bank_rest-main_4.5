CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE IF NOT EXISTS roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255)       NOT NULL
);
CREATE TABLE IF NOT EXISTS users_roles
(
    user_id BIGINT  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE IF NOT EXISTS cards
(
    id                    BIGSERIAL PRIMARY KEY,
    encrypted_card_number TEXT UNIQUE      NOT NULL,
    owner_name            VARCHAR(50)      NOT NULL,
    expiration_date       DATE             NOT NULL,
    status                VARCHAR(20)      NOT NULL,
    balance               DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    user_id               BIGINT           NOT NULL REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS transactions
(
    id           BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT           NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    to_card_id   BIGINT           NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    amount       DOUBLE PRECISION NOT NULL,
    timestamp    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)      NOT NULL
);
INSERT INTO roles (name)
VALUES ('USER')
ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name)
VALUES ('ADMIN')
ON CONFLICT (name) DO NOTHING;
INSERT INTO users (username, password)
VALUES ('user', '$2a$12$.e.FugbBEPzCDxLsZEA5BeRpD.gfvnrMB3CiqwGfDKY7HuqJDfjUG')
ON CONFLICT (username) DO NOTHING;
INSERT INTO users (username, password)
VALUES ('admin', '$2a$12$KTh8bU.CtA/7eHQum36wo.SwaTgs6n.c1s26qReAabmsF4YN5cbMy')
ON CONFLICT (username) DO NOTHING;
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.username = 'user'
  AND r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.username = 'admin'
  AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
DO
$$
    BEGIN
        RAISE NOTICE 'Создано пользователей: %', (SELECT COUNT(*) FROM users);
        RAISE NOTICE 'Назначено ролей: %', (SELECT COUNT(*) FROM users_roles);
    END
$$;