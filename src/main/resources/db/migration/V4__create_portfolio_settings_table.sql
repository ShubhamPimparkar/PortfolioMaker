CREATE TABLE IF NOT EXISTS portfolio_settings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    template_key VARCHAR(50) NOT NULL DEFAULT 'classic',
    primary_color VARCHAR(7),
    font_family VARCHAR(100),
    show_skills BOOLEAN NOT NULL DEFAULT TRUE,
    show_projects BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_portfolio_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_portfolio_settings_user_id ON portfolio_settings(user_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_settings_template_key ON portfolio_settings(template_key);

