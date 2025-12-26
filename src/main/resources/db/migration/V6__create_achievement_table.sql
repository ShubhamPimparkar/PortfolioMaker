CREATE TABLE IF NOT EXISTS achievement (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    issue_date DATE,
    description TEXT,
    link VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_achievement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_achievement_user_id ON achievement(user_id);
CREATE INDEX IF NOT EXISTS idx_achievement_issue_date ON achievement(issue_date DESC);

