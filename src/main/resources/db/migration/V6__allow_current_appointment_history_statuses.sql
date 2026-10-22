-- Existing deployments may have a Hibernate-generated check constraint that
-- predates the current appointment workflow. Replace history status checks so
-- every status currently used by AppointmentStatus can be audited.
DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE n.nspname = current_schema()
          AND t.relname = 'appointment_status_history'
          AND c.contype = 'c'
          AND (
              pg_get_constraintdef(c.oid) ILIKE '%from_status%'
              OR pg_get_constraintdef(c.oid) ILIKE '%to_status%'
          )
    LOOP
        EXECUTE format(
            'ALTER TABLE appointment_status_history DROP CONSTRAINT %I',
            constraint_name
        );
    END LOOP;
END $$;

ALTER TABLE appointment_status_history
    ADD CONSTRAINT appointment_status_history_from_status_allowed
    CHECK (from_status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));

ALTER TABLE appointment_status_history
    ADD CONSTRAINT appointment_status_history_to_status_allowed
    CHECK (to_status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));
