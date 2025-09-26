INSERT INTO cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES ('encrypted_1231111111111111',
        'Ivan Ivanov',
        '2026-12-31'::DATE,
        'ACTIVE',
        1000.0,
        1)
ON CONFLICT (encrypted_card_number) DO NOTHING;
INSERT INTO cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES ('encrypted_2345555555554444',
        'Anna Petrova',
        '2025-06-30'::DATE,
        'ACTIVE',
        2000.0,
        1)
ON CONFLICT (encrypted_card_number) DO NOTHING;
INSERT INTO cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES ('encrypted_345282246310005',
        'Mikhail Sidorov',
        '2027-03-31'::DATE,
        'ACTIVE',
        1500.0,
        1)
ON CONFLICT (encrypted_card_number) DO NOTHING;
INSERT INTO cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES ('encrypted_4561111111112222',
        'Dmitry Kuznetsov',
        '2026-09-30'::DATE,
        'ACTIVE',
        5000.0,
        2)
ON CONFLICT (encrypted_card_number) DO NOTHING;
INSERT INTO cards (encrypted_card_number, owner_name, expiration_date, status, balance, user_id)
VALUES ('encrypted_5675555555553333',
        'Elena Smirnova',
        '2025-12-31'::DATE,
        'ACTIVE',
        3000.0,
        2)
ON CONFLICT (encrypted_card_number) DO NOTHING;
