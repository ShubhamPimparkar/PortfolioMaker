-- Update analytics_event_type enum to remove BOUNCE (it's now derived server-side)
-- Note: We cannot directly remove enum values in PostgreSQL, so we'll create a new enum
-- and migrate existing data. BOUNCE events will be ignored going forward.

-- Add new columns for enhanced tracking
ALTER TABLE portfolio_analytics_event 
    ADD COLUMN IF NOT EXISTS scroll_depth INTEGER,
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(512);

-- Create composite indexes for de-duplication and efficient queries
CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_event_user_visitor_created 
    ON portfolio_analytics_event(portfolio_user_id, visitor_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_portfolio_analytics_event_type_created 
    ON portfolio_analytics_event(event_type, created_at DESC);

-- Note: The BOUNCE enum value remains in the database enum type for backward compatibility
-- but will not be used in new events. The application code filters it out.

