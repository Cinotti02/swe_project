-- =========================================
-- SEED DI BASE PER DINEUP (PostgreSQL)
-- =========================================

-- ⚠️ OPZIONALE: pulisci tutte le tabelle (ordine importante per i vincoli)
-- ATTENZIONE: questo cancella tutti i dati esistenti!
-- TRUNCATE TABLE merge_tables, order_items, orders, reservations,
--               notifications, tables, slots, dishes, categories, users
-- RESTART IDENTITY CASCADE;

-- ===========================
-- 1) USERS (OWNER, STAFF, CUSTOMER)
-- ===========================
-- Ruoli coerenti con enum Java: OWNER, STAFF, CUSTOMER

INSERT INTO users (username, email, password_hash, fidelity_points, name, surname, role)
VALUES
    -- OWNER
    ('owner1',
     'owner@example.com',
     'owner_hashed_pwd',   -- sostituisci con hash reale se usi bcrypt/altro
     0,
     'Mario',
     'Rossi',
     'OWNER'),

    -- STAFF
    ('staff1',
     'staff@example.com',
     'staff_hashed_pwd',
     0,
     'Lucia',
     'Bianchi',
     'STAFF'),

    -- CUSTOMER DI TEST
    ('customer1',
     'customer@example.com',
     'customer_hashed_pwd',
     100,
     'Francesco',
     'Verdi',
     'CUSTOMER');

-- ===========================
-- 2) CATEGORIES
-- ===========================
INSERT INTO categories (name, description, active)
VALUES
    ('Pizze',    'Pizze classiche e speciali', TRUE),
    ('Bevande',  'Acqua, bibite, birre',       TRUE),
    ('Dessert',  'Dolci della casa',           TRUE);

-- ===========================
-- 3) DISHES
-- ===========================
INSERT INTO dishes (name, description, price, active, category_id)
VALUES
    ('Margherita',
     'Pomodoro, mozzarella, basilico',
     6.50,
     TRUE,
     (SELECT id FROM categories WHERE name = 'Pizze')),

    ('Diavola',
     'Pomodoro, mozzarella, salamino piccante',
     8.00,
     TRUE,
     (SELECT id FROM categories WHERE name = 'Pizze')),

    ('Acqua naturale 0.75L',
     'Bottiglia da 0.75L',
     2.00,
     TRUE,
     (SELECT id FROM categories WHERE name = 'Bevande')),

    ('Birra chiara 0.4L',
     'Birra alla spina',
     4.50,
     TRUE,
     (SELECT id FROM categories WHERE name = 'Bevande')),

    ('Tiramisù',
     'Tiramisù della casa',
     5.00,
     TRUE,
     (SELECT id FROM categories WHERE name = 'Dessert'));

-- ===========================
-- 4) TABLES (tavoli fisici)
-- ===========================
INSERT INTO tables (number, seats, joinable, location, available)
VALUES
    (1, 2,  TRUE, 'Interno',  TRUE),
    (2, 4,  TRUE, 'Interno',  TRUE),
    (3, 4,  TRUE, 'Esterno',  TRUE),
    (4, 6,  TRUE, 'Interno',  TRUE),
    (5, 2,  FALSE,'Esterno',  TRUE);

-- ===========================
-- 5) SLOTS (fasce orarie)
-- ===========================
INSERT INTO slots (start_time, end_time, closed)
VALUES
    ('19:00', '21:00', FALSE),
    ('21:00', '23:00', FALSE);

-- ===========================
-- 6) RESERVATIONS
-- ===========================
-- Attenzione: i valori di "status" devono combaciare con il tuo enum ReservationStatus
-- Valori attuali: CREATED, CONFIRMED, CHECKED_IN, COMPLETED, NO_SHOW, CANCELED

INSERT INTO reservations (customer_id, guests, reservation_date, slot_id, status, notes)
VALUES
    (
        (SELECT id FROM users WHERE username = 'customer1'),
        4,
        '2025-01-01',
        (SELECT id FROM slots WHERE start_time = '19:00' AND end_time = '21:00'),
        'CONFIRMED',
        'Prenotazione di prova: 4 persone, tavoli uniti'
    ),
    (
        (SELECT id FROM users WHERE username = 'customer1'),
        2,
        '2025-01-02',
        (SELECT id FROM slots WHERE start_time = '21:00' AND end_time = '23:00'),
        'CREATED',
        'Prenotazione di prova: 2 persone'
    );

-- ===========================
-- 7) MERGE_TABLES (tavoli uniti per una prenotazione)
-- ===========================
-- Uniamo i tavoli 2 e 3 per la prenotazione del 2025-01-01 (4 persone)

INSERT INTO merge_tables (reservation_id, table_id, seats_assigned, merged_group_id)
VALUES
    (
        (SELECT r.id FROM reservations r
                              JOIN users u ON r.customer_id = u.id
         WHERE u.username = 'customer1'
           AND r.reservation_date = '2025-01-01'),
        (SELECT id FROM tables WHERE number = 2),
        2,
        'G1'
    ),
    (
        (SELECT r.id FROM reservations r
                              JOIN users u ON r.customer_id = u.id
         WHERE u.username = 'customer1'
           AND r.reservation_date = '2025-01-01'),
        (SELECT id FROM tables WHERE number = 3),
        2,
        'G1'
    );

-- ===========================
-- 8) ORDERS
-- ===========================
-- status e payment_method devono combaciare con i tuoi enum OrderStatus e PaymentMethod
-- Valori attuali:
--   status: CREATED, PREPARING, READY, RETIRED, CANCELLED
--   payment_method: ONLINE, IN_LOCO

INSERT INTO orders (customer_id, created_at, status, payment_method, total_amount, notes)
VALUES
    (
        (SELECT id FROM users WHERE username = 'customer1'),
        '2025-01-01 19:30:00',
        'CREATED',
        'IN_LOCO',
        18.50,
        'Ordine di prova: pizza + birra'
    ),
    (
        (SELECT id FROM users WHERE username = 'customer1'),
        '2025-01-02 21:15:00',
        'RETIRED',
        'ONLINE',
        15.00,
        'Ordine di prova: pizza + acqua + dessert'
    );

-- ===========================
-- 9) ORDER_ITEMS
-- ===========================
INSERT INTO order_items (order_id, dish_id, unit_price, quantity)
VALUES
    -- Ordine 1: Margherita + 2 Birre
    (
        (SELECT id FROM orders WHERE notes LIKE 'Ordine di prova: pizza + birra' LIMIT 1),
    (SELECT id FROM dishes WHERE name = 'Margherita'),
    6.50,
    1
    ),
    (
        (SELECT id FROM orders WHERE notes LIKE 'Ordine di prova: pizza + birra' LIMIT 1),
        (SELECT id FROM dishes WHERE name = 'Birra chiara 0.4L'),
        4.50,
        2
    ),

    -- Ordine 2: Diavola + Acqua + Tiramisù
    (
        (SELECT id FROM orders WHERE notes LIKE 'Ordine di prova: pizza + acqua + dessert' LIMIT 1),
        (SELECT id FROM dishes WHERE name = 'Diavola'),
        8.00,
        1
    ),
    (
        (SELECT id FROM orders WHERE notes LIKE 'Ordine di prova: pizza + acqua + dessert' LIMIT 1),
        (SELECT id FROM dishes WHERE name = 'Acqua naturale 0.75L'),
        2.00,
        1
    ),
    (
        (SELECT id FROM orders WHERE notes LIKE 'Ordine di prova: pizza + acqua + dessert' LIMIT 1),
        (SELECT id FROM dishes WHERE name = 'Tiramisù'),
        5.00,
        1
    );

-- ===========================
-- 10) NOTIFICATIONS
-- ===========================
-- Valori attuali:
--   type: CONFIRMATION, REMINDER, UPDATE, ALERT
--   status: SENT, READ, FAILED

INSERT INTO notifications (recipient_id, message, type, status, created_at, read_at)
VALUES
    (
        (SELECT id FROM users WHERE username = 'staff1'),
        'Nuova prenotazione per il 1 gennaio (4 ospiti).',
        'ALERT',
        'SENT',
        NOW(),
        NULL
    ),
    (
        (SELECT id FROM users WHERE username = 'owner1'),
        'Ordine del 1 gennaio completato e pagato.',
        'UPDATE',
        'READ',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '23 hours'
    );
