-- Run once against existing databases before deploying the three-state application.
-- CONFIRMED represented an accepted/in-progress visit in the old UI.
UPDATE appointment SET status = 'IN_PROGRESS' WHERE status = 'CONFIRMED';
-- Existing CANCELLED appointments remain read-only legacy records.

-- Hibernate ddl-auto=update adds the nullable profile links and status history table
-- (with the other columns) on application startup.
