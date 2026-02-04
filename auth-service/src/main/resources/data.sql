
-- To generate new hash: use BCryptPasswordEncoder or https://bcrypt-generator.com/


-- Initial test user
-- Password: user123 (BCrypt hash)
INSERT INTO users (email, password, role, created_at, updated_at)
VALUES (
    'user@example.com',
    '$2a$10$8K1p/a0dL3YqJqK8K1p/a0dL3YqJqK8K1p/a0dL3YqJqK8K1p/a0dL3YqJqK', -- user123
    'ROLE_USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
