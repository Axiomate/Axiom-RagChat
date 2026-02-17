-- V3: Add composite index for message queries (no partitioning — keeps JPA mapping intact)
CREATE INDEX idx_message_session_created
    ON chat_messages(session_id, created_at);

-- Partition-ready function for future use (does not alter the table)
CREATE OR REPLACE FUNCTION create_message_partition_stub()
RETURNS void AS $$
BEGIN
    RAISE NOTICE 'Partitioning can be enabled in a future migration once data volume requires it.';
END;
$$ LANGUAGE plpgsql;

COMMENT ON TABLE chat_messages
    IS 'Chat messages table — partition-ready via session_id + created_at index';