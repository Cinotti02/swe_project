UPDATE notifications
SET status = 'SENT',
    read_at = NULL
WHERE status NOT IN ('SENT', 'READ', 'FAILED');
