-- ============================================================
-- TaskMaster Sample Data
-- Run AFTER the application starts (Hibernate auto-creates tables)
-- All passwords are BCrypt hash of: password123
-- ============================================================

INSERT IGNORE INTO users (username, email, password, full_name, bio, role, enabled)
VALUES
('alice', 'alice@example.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Alice Johnson', 'Senior Backend Engineer', 'USER', true),
('bob', 'bob@example.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Bob Smith', 'Frontend Developer', 'USER', true),
('carol', 'carol@example.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'Carol White', 'QA Engineer', 'USER', true),
('admin', 'admin@taskmaster.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 'System Admin', 'Platform Administrator', 'ADMIN', true);

INSERT IGNORE INTO teams (name, description, owner_id, invite_code)
VALUES
('Alpha Team',   'Core product development team', 1, 'ALPHA12345'),
('Beta Testers', 'QA and testing team',           3, 'BETA678910');

INSERT IGNORE INTO team_members (team_id, user_id)
VALUES (1, 1), (1, 2), (1, 3), (2, 3), (2, 4);

INSERT IGNORE INTO tasks (title, description, status, priority, due_date, creator_id, assignee_id, team_id)
VALUES
('Setup CI/CD pipeline',
 'Configure GitHub Actions for automated build and deployment to staging',
 'OPEN', 'HIGH', '2026-04-15', 1, 2, 1),
('Design database schema',
 'Create full ERD and MySQL schema for the v2.0 release',
 'IN_PROGRESS', 'URGENT', '2026-04-10', 1, 1, 1),
('Write unit tests',
 'Cover the entire service layer with JUnit 5 and Mockito',
 'OPEN', 'MEDIUM', '2026-04-20', 3, 3, 2),
('Fix login bug',
 'JWT token refresh fails silently after 24 hours — trace root cause',
 'REVIEW', 'HIGH', '2026-04-08', 2, 1, 1),
('Update API documentation',
 'Add all new v1.3 endpoints to the README and Swagger descriptions',
 'COMPLETED', 'LOW', '2026-04-05', 1, 2, 1);

INSERT IGNORE INTO comments (content, task_id, author_id)
VALUES
('I will start on the CI/CD setup tomorrow morning.', 1, 2),
('Please prioritize this — it is blocking the release pipeline.', 1, 1),
('Schema draft is ready for team review in the shared folder.', 2, 1),
('Found the root cause: missing token rotation on the refresh endpoint.', 4, 1),
('Tests for UserService and TaskService are now complete.', 3, 3);
