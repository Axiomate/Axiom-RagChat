CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    preference_summary TEXT,
    total_sessions     INTEGER   DEFAULT 0,
    total_messages     BIGINT    DEFAULT 0,
    enabled            BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email   ON users(email);
CREATE INDEX idx_user_created ON users(created_at);

CREATE TABLE chat_sessions (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(500)  NOT NULL,
    rolling_summary   TEXT,
    final_summary     TEXT,
    session_snapshot  TEXT,
    message_count     INTEGER       DEFAULT 0,
    last_summary_at   TIMESTAMP,
    total_tokens      BIGINT        DEFAULT 0,
    status            VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE',
    last_activity_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at          TIMESTAMP
);

CREATE INDEX idx_session_user          ON chat_sessions(user_id);
CREATE INDEX idx_session_status        ON chat_sessions(status);
CREATE INDEX idx_session_updated       ON chat_sessions(updated_at);
CREATE INDEX idx_session_last_activity ON chat_sessions(last_activity_at);

CREATE TABLE chat_messages (
    id            BIGSERIAL PRIMARY KEY,
    session_id    BIGINT       NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role          VARCHAR(50)  NOT NULL,
    content       TEXT         NOT NULL,
    token_count   INTEGER,
    embedding_id  VARCHAR(255),
    metadata      JSONB,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_session   ON chat_messages(session_id);
CREATE INDEX idx_message_created   ON chat_messages(created_at);
CREATE INDEX idx_message_embedding ON chat_messages(embedding_id);

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sessions_updated_at
    BEFORE UPDATE ON chat_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();