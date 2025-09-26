DROP SCHEMA IF EXISTS test CASCADE;
CREATE SCHEMA IF NOT EXISTS test;
CREATE TABLE IF NOT EXISTS test.roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS test.users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255)       NOT NULL
);
CREATE TABLE IF NOT EXISTS test.users_roles
(
    user_id BIGINT  NOT NULL REFERENCES test.users (id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES test.roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE TABLE IF NOT EXISTS test.cards
(
    id                    BIGSERIAL PRIMARY KEY,
    encrypted_card_number TEXT UNIQUE      NOT NULL,
    owner_name            VARCHAR(50)      NOT NULL,
    expiration_date       DATE             NOT NULL,
    status                VARCHAR(20)      NOT NULL,
    balance               DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    user_id               BIGINT           NOT NULL REFERENCES test.users (id)
);
CREATE TABLE IF NOT EXISTS test.transactions
(
    id           BIGSERIAL PRIMARY KEY,
    from_card_id BIGINT           NOT NULL REFERENCES test.cards (id) ON DELETE CASCADE,
    to_card_id   BIGINT           NOT NULL REFERENCES test.cards (id) ON DELETE CASCADE,
    amount       DOUBLE PRECISION NOT NULL,
    timestamp    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)      NOT NULL
);
INSERT INTO test.roles (name)
VALUES ('USER');
INSERT INTO test.roles (name)
VALUES ('ADMIN');
INSERT INTO test.users (username, password)
VALUES ('user', '$2a$12$.e.FugbBEPzCDxLsZEA5BeRpD.gfvnrMB3CiqwGfDKY7HuqJDfjUG');
INSERT INTO test.users (username, password)
VALUES ('admin', '$2a$12$KTh8bU.CtA/7eHQum36wo.SwaTgs6n.c1s26qReAabmsF4YN5cbMy');
INSERT INTO test.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM test.users u,
     test.roles r
WHERE u.username = 'user'
  AND r.name = 'USER';
INSERT INTO test.users_roles (user_id, role_id)
SELECT u.id, r.id
FROM test.users u,
     test.roles r
WHERE u.username = 'admin'
  AND r.name = 'ADMIN';