-- Fix ticket officer user and ensure proper role assignment
-- This script ensures the ticket officer user exists with the correct role

-- First, ensure the role column exists
ALTER TABLE passenger ADD COLUMN IF NOT EXISTS role ENUM('PASSENGER', 'TRAIN_STATION_MASTER', 'TICKET_OFFICER', 'PASSENGER_EXPERIENCE_ANALYST', 'ADMIN_STAFF') DEFAULT 'PASSENGER';

-- Insert or update the ticket officer user
INSERT INTO passenger (id, full_name, email, phone, username, password, role) VALUES
(6, 'Ticket Officer', 'ticket.officer@train.com', '+94771234572', 'ticketofficer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'TICKET_OFFICER')
ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name),
    email = VALUES(email),
    phone = VALUES(phone),
    username = VALUES(username),
    password = VALUES(password),
    role = VALUES(role);

-- Update existing ticket officer user if it exists without role
UPDATE passenger SET role = 'TICKET_OFFICER' WHERE username = 'ticketofficer';

-- Verify the ticket officer user
SELECT id, username, full_name, role FROM passenger WHERE username = 'ticketofficer';

-- Show all users and their roles
SELECT id, username, full_name, role FROM passenger ORDER BY id;
