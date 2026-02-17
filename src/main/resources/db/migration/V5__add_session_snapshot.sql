-- Session snapshot metadata
ALTER TABLE chat_sessions
    ADD COLUMN IF NOT EXISTS snapshot_created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS snapshot_version    INTEGER DEFAULT 1;

-- Session restart history
CREATE TABLE session_restart_history (
    id                   BIGSERIAL PRIMARY KEY,
    original_session_id  BIGINT       NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    new_session_id       BIGINT       NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    restarted_by_user_id BIGINT       NOT NULL REFERENCES users(id),
    restart_reason       VARCHAR(255),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restart_original ON session_restart_history(original_session_id);
CREATE INDEX idx_restart_new       ON session_restart_history(new_session_id);
CREATE INDEX idx_restart_user      ON session_restart_history(restarted_by_user_id);

COMMENT ON TABLE  session_restart_history         IS 'Tracks session restart events';
COMMENT ON COLUMN chat_sessions.snapshot_created_at IS 'Timestamp when snapshot was last created';
COMMENT ON COLUMN chat_sessions.snapshot_version    IS 'Snapshot format version for compatibility';