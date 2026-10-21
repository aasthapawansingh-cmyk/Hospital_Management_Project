-- Run once against existing databases before deploying the controlled workflow.
UPDATE appointment SET status = 'PENDING' WHERE status IS NULL;
-- Existing CONFIRMED and CANCELLED appointments are preserved as-is.

-- Hibernate ddl-auto=update adds the nullable profile links and status history table
-- (with the other columns) on application startup.
