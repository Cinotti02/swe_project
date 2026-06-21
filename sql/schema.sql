-- SCHEMA DINEUP - PostgreSQL

-- 1) USERS
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username        VARCHAR(50)  NOT NULL,
                       email           VARCHAR(100) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       fidelity_points INT          DEFAULT 0 CHECK (fidelity_points >= 0),
                       name            VARCHAR(50),
                       surname         VARCHAR(50),
                       role            VARCHAR(20)  NOT NULL CHECK (role IN ('OWNER', 'STAFF', 'CUSTOMER'))
);

-- 2) CATEGORIES
CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name        VARCHAR(50) NOT NULL UNIQUE,
                            description TEXT,
                            active      BOOLEAN     DEFAULT TRUE
);

-- 3) DISHES
CREATE TABLE dishes (
                        id SERIAL PRIMARY KEY,
                        name        VARCHAR(100) NOT NULL,
                        description TEXT,
                        price       NUMERIC(10,2) NOT NULL CHECK (price >= 0),
                        active      BOOLEAN       DEFAULT TRUE,
                        category_id INT REFERENCES categories(id) ON DELETE SET NULL
);

-- 4) TABLES (tavoli fisici)
CREATE TABLE tables (
                        id SERIAL PRIMARY KEY,
                        number    INT         NOT NULL UNIQUE,
                        seats     INT         NOT NULL CHECK (seats > 0),
                        joinable  BOOLEAN     DEFAULT TRUE,
                        location  VARCHAR(50),
                        available BOOLEAN     DEFAULT TRUE
);

-- 5) SLOTS (fasce orarie)
CREATE TABLE slots (
                       id SERIAL PRIMARY KEY,
                       start_time TIME    NOT NULL,
                       end_time   TIME    NOT NULL,
                       closed     BOOLEAN DEFAULT FALSE,
                       CHECK (start_time < end_time)
);

-- 6) RESERVATIONS
CREATE TABLE reservations (
                              id SERIAL PRIMARY KEY,
                              customer_id      INT        NOT NULL,
                              guests           INT        NOT NULL CHECK (guests > 0),
                              reservation_date DATE       NOT NULL,
                              slot_id          INT        NOT NULL,
                              status           VARCHAR(20) NOT NULL CHECK (status IN ('CREATED', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'NO_SHOW', 'CANCELED')),
                              notes            TEXT,

                              FOREIGN KEY (customer_id) REFERENCES users(id),
                              FOREIGN KEY (slot_id)     REFERENCES slots(id)
);

-- 7) ORDERS
CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        customer_id    INT         NOT NULL,
                        created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
                        status         VARCHAR(20) NOT NULL CHECK (status IN ('CREATED', 'PREPARING', 'READY', 'RETIRED', 'CANCELLED')),
                        payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('ONLINE', 'IN_LOCO')),
                        total_amount   NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
                        notes          TEXT,

                        FOREIGN KEY (customer_id)    REFERENCES users(id)
);

-- 8) ORDER ITEMS
CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id    INT         NOT NULL,
                             dish_id     INT         NOT NULL,
                             unit_price  NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
                             quantity    INT         NOT NULL CHECK (quantity > 0),
                             total_price NUMERIC(10,2) GENERATED ALWAYS AS (unit_price * quantity) STORED,

                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (dish_id)  REFERENCES dishes(id)
);

-- 9) NOTIFICATIONS
CREATE TABLE notifications (
                               id SERIAL PRIMARY KEY,
                               recipient_id INT         NOT NULL,
                               message      TEXT        NOT NULL,
                               type         VARCHAR(20) NOT NULL CHECK (type IN ('CONFIRMATION', 'REMINDER', 'UPDATE', 'ALERT')),
                               status       VARCHAR(20) NOT NULL CHECK (status IN ('SENT', 'READ', 'FAILED')),
                               created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
                               read_at      TIMESTAMP,

                               FOREIGN KEY (recipient_id) REFERENCES users(id)
);

-- 10) MERGE_TABLES (tavoli uniti per una prenotazione)
CREATE TABLE merge_tables (
                              id SERIAL PRIMARY KEY,
                              reservation_id INT NOT NULL,
                              table_id       INT NOT NULL,
                              seats_assigned INT NOT NULL CHECK (seats_assigned > 0),
                              merged_group_id VARCHAR(50) NOT NULL,

                              FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
                              FOREIGN KEY (table_id)       REFERENCES tables(id)
);

-- Impedisce che lo stesso tavolo venga assegnato a due prenotazioni attive
-- nello stesso giorno e slot, anche in presenza di richieste concorrenti.
CREATE OR REPLACE FUNCTION prevent_double_table_booking()
RETURNS TRIGGER AS $$
DECLARE
    booking_date DATE;
    booking_slot INT;
BEGIN
    SELECT reservation_date, slot_id
    INTO booking_date, booking_slot
    FROM reservations
    WHERE id = NEW.reservation_id;

    -- Serializza le assegnazioni concorrenti relative allo stesso giorno/slot.
    PERFORM pg_advisory_xact_lock(
        hashtextextended(booking_date::TEXT || ':' || booking_slot::TEXT, 0)
    );

    IF EXISTS (
        SELECT 1
        FROM merge_tables mt
        JOIN reservations r ON r.id = mt.reservation_id
        WHERE mt.table_id = NEW.table_id
          AND mt.reservation_id <> NEW.reservation_id
          AND r.reservation_date = booking_date
          AND r.slot_id = booking_slot
          AND r.status NOT IN ('CANCELED', 'NO_SHOW', 'COMPLETED')
    ) THEN
        RAISE EXCEPTION
            'Table % is already booked for % in slot %',
            NEW.table_id, booking_date, booking_slot;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_double_table_booking
BEFORE INSERT OR UPDATE OF reservation_id, table_id
ON merge_tables
FOR EACH ROW
EXECUTE FUNCTION prevent_double_table_booking();

CREATE INDEX idx_reservations_date_slot_status
    ON reservations(reservation_date, slot_id, status);

CREATE INDEX idx_merge_tables_table_reservation
    ON merge_tables(table_id, reservation_id);
