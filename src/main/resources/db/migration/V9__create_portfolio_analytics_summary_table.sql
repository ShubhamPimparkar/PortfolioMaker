CREATE TABLE IF NOT EXISTS portfolio_analytics_summary (
    portfolio_user_id UUID PRIMARY KEY,
    total_views INTEGER NOT NULL DEFAULT 0,
    engaged_views INTEGER NOT NULL DEFAULT 0,
    bounce_count INTEGER NOT NULL DEFAULT 0,
    avg_duration_seconds INTEGER NOT NULL DEFAULT 0,
    last_calculated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_portfolio_analytics_summary_user FOREIGN KEY (portfolio_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_summary_last_calculated ON portfolio_analytics_summary(last_calculated_at DESC);

