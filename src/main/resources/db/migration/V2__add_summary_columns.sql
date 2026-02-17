-- Indexes for summary operations
CREATE INDEX idx_session_final_summary
    ON chat_sessions(user_id)
    WHERE final_summary IS NOT NULL;

CREATE INDEX idx_session_user_status_updated
    ON chat_sessions(user_id, status, updated_at DESC);

CREATE INDEX idx_message_metadata
    ON chat_messages USING GIN(metadata);

COMMENT ON COLUMN chat_sessions.rolling_summary
    IS 'Incrementally updated summary during active session';
COMMENT ON COLUMN chat_sessions.final_summary
    IS 'Comprehensive summary generated on session end';
COMMENT ON COLUMN chat_sessions.session_snapshot
    IS 'JSON snapshot for session restart capability';
COMMENT ON COLUMN users.preference_summary
    IS 'Aggregated user preferences across all sessions';