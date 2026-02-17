-- Token tracking columns
ALTER TABLE chat_sessions
    ADD COLUMN IF NOT EXISTS prompt_tokens     BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS completion_tokens BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS embedding_tokens  BIGINT DEFAULT 0;

-- Session analytics view
CREATE OR REPLACE VIEW session_analytics AS
SELECT
    s.id                                                                      AS session_id,
    s.user_id,
    s.title,
    s.status,
    s.message_count,
    s.total_tokens,
    s.prompt_tokens,
    s.completion_tokens,
    s.embedding_tokens,
    COUNT(m.id)                                                               AS actual_message_count,
    COALESCE(SUM(m.token_count), 0)                                          AS calculated_tokens,
    s.created_at,
    s.ended_at,
    EXTRACT(EPOCH FROM (COALESCE(s.ended_at, CURRENT_TIMESTAMP) - s.created_at))
        / 3600                                                                AS duration_hours
FROM chat_sessions s
LEFT JOIN chat_messages m ON s.id = m.session_id
GROUP BY s.id;

-- User analytics view
CREATE OR REPLACE VIEW user_analytics AS
SELECT
    u.id                                    AS user_id,
    u.email,
    u.name,
    u.total_sessions,
    u.total_messages,
    COUNT(DISTINCT s.id)                    AS actual_session_count,
    COUNT(m.id)                             AS actual_message_count,
    COALESCE(SUM(s.total_tokens), 0)        AS total_tokens_used,
    COALESCE(AVG(s.message_count), 0)       AS avg_messages_per_session,
    u.created_at                            AS user_since
FROM users u
LEFT JOIN chat_sessions s ON u.id = s.user_id
LEFT JOIN chat_messages m ON s.id = m.session_id
GROUP BY u.id;

CREATE INDEX idx_session_analytics ON chat_sessions(user_id, created_at, status);