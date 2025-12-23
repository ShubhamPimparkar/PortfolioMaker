CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    project_url VARCHAR(512),
    github_repo_url VARCHAR(512),
    role VARCHAR(255),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_projects_user_id ON projects(user_id);
CREATE INDEX IF NOT EXISTS idx_projects_is_public ON projects(is_public);
CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects(created_at DESC);

-- Create tech stack collection table
CREATE TABLE IF NOT EXISTS projects_tech_stack (
    project_id UUID NOT NULL,
    tech VARCHAR(100) NOT NULL,
    CONSTRAINT fk_projects_tech_stack_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, tech)
);

CREATE INDEX IF NOT EXISTS idx_projects_tech_stack_project_id ON projects_tech_stack(project_id);

