-- Run this once before using the updated app against an existing PostgreSQL database.
-- It replaces the old report status labels with the lost-and-found workflow labels:
-- LOST, FOUND, CLAIMED, RESOLVED.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'report_status') THEN
        ALTER TYPE report_status RENAME TO report_status_old;
        CREATE TYPE report_status AS ENUM ('LOST', 'FOUND', 'CLAIMED', 'RESOLVED');

        ALTER TABLE item_reports
            ALTER COLUMN status DROP DEFAULT,
            ALTER COLUMN status TYPE report_status
                USING (
                    CASE status::text
                        WHEN 'Unclaimed' THEN 'LOST'
                        WHEN 'Pending' THEN 'LOST'
                        WHEN 'Lost' THEN 'LOST'
                        WHEN 'LOST' THEN 'LOST'
                        WHEN 'Found' THEN 'FOUND'
                        WHEN 'FOUND' THEN 'FOUND'
                        WHEN 'Claimed' THEN 'CLAIMED'
                        WHEN 'CLAIMED' THEN 'CLAIMED'
                        WHEN 'Approved' THEN 'CLAIMED'
                        WHEN 'Archived' THEN 'RESOLVED'
                        WHEN 'Resolved' THEN 'RESOLVED'
                        WHEN 'RESOLVED' THEN 'RESOLVED'
                        ELSE 'LOST'
                    END
                )::report_status;

        DROP TYPE report_status_old;
    ELSE
        CREATE TYPE report_status AS ENUM ('LOST', 'FOUND', 'CLAIMED', 'RESOLVED');
    END IF;
END $$;
