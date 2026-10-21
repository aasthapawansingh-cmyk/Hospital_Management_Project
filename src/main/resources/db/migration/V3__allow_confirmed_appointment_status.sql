-- Hibernate may have generated a CHECK constraint for the old string enum.
-- Replace any appointment status checks so CONFIRMED can be persisted.
DO $$
DECLARE constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        WHERE t.relname = 'appointment'
          AND c.contype = 'c'
          AND pg_get_constraintdef(c.oid) ILIKE '%status%'
    LOOP
        EXECUTE format('ALTER TABLE appointment DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE appointment ADD CONSTRAINT appointment_status_allowed
    CHECK (status IS NULL OR status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));
ALTER TABLE appointment ALTER COLUMN status SET NOT NULL;
