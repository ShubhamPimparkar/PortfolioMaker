CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    headline VARCHAR(255) NOT NULL,
    summary TEXT,
    location VARCHAR(255),
    years_of_experience INTEGER,
    github_url VARCHAR(512),
    linkedin_url VARCHAR(512),
    portfolio_url VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);

-- Create skills collection table
CREATE TABLE IF NOT EXISTS profiles_skills (
    profile_id UUID NOT NULL,
    skill VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profiles_skills_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, skill)
);

CREATE INDEX IF NOT EXISTS idx_profiles_skills_profile_id ON profiles_skills(profile_id);

