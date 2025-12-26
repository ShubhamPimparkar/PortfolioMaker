DO $$ BEGIN
    CREATE TYPE analytics_event_type AS ENUM ('VIEW', 'ENGAGED', 'BOUNCE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS portfolio_analytics_event (
    id UUID PRIMARY KEY,
    portfolio_user_id UUID NOT NULL,
    visitor_id VARCHAR(255) NOT NULL,
    event_type analytics_event_type NOT NULL,
    duration_seconds INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_portfolio_analytics_event_user FOREIGN KEY (portfolio_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_event_user_id ON portfolio_analytics_event(portfolio_user_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_event_created_at ON portfolio_analytics_event(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_event_type ON portfolio_analytics_event(event_type);

