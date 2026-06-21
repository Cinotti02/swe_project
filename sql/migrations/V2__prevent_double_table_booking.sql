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

DROP TRIGGER IF EXISTS check_double_table_booking ON merge_tables;

CREATE TRIGGER check_double_table_booking
BEFORE INSERT OR UPDATE OF reservation_id, table_id
ON merge_tables
FOR EACH ROW
EXECUTE FUNCTION prevent_double_table_booking();

CREATE INDEX IF NOT EXISTS idx_reservations_date_slot_status
    ON reservations(reservation_date, slot_id, status);

CREATE INDEX IF NOT EXISTS idx_merge_tables_table_reservation
    ON merge_tables(table_id, reservation_id);
