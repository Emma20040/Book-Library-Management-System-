


INSERT INTO library_management_system.users (id, username, email, password, enabled, role, verification_token)
VALUES (
    UUID_TO_BIN(UUID()), 
    'admin1', 
    'admin1@gmail.com', 
    '$2a$10$/brMdqlQmpER1.7iYPZRjOzT/s96HALanVz0o5wha1bbO0sXATEEu', 
    true, 
    'ADMIN', 
    NULL
);