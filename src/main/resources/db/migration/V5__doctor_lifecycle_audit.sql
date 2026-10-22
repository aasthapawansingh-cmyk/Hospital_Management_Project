-- Apply once to an existing PostgreSQL deployment before running the updated app.
ALTER TABLE doctor ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE auth_user ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE appointment_status_history ADD COLUMN IF NOT EXISTS reason VARCHAR(1000);

CREATE TABLE IF NOT EXISTS doctor_status_history (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id),
    active BOOLEAN NOT NULL,
    actor_username VARCHAR(255) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment_assignment_history (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointment(id),
    from_doctor_id BIGINT NOT NULL REFERENCES doctor(id),
    to_doctor_id BIGINT NOT NULL REFERENCES doctor(id),
    actor_username VARCHAR(255) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment_schedule_history (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointment(id),
    from_doctor_id BIGINT NOT NULL REFERENCES doctor(id),
    to_doctor_id BIGINT NOT NULL REFERENCES doctor(id),
    from_date DATE NOT NULL,
    from_time TIME NOT NULL,
    to_date DATE NOT NULL,
    to_time TIME NOT NULL,
    actor_username VARCHAR(255) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);
