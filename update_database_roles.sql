-- Update database to add role column if it doesn't exist
ALTER TABLE passenger ADD COLUMN IF NOT EXISTS role ENUM('PASSENGER', 'TRAIN_STATION_MASTER', 'TICKET_OFFICER', 'PASSENGER_EXPERIENCE_ANALYST', 'ADMIN_STAFF') DEFAULT 'PASSENGER';

-- Update existing users with their roles
UPDATE passenger SET role = 'ADMIN_STAFF' WHERE username = 'admin';
UPDATE passenger SET role = 'TRAIN_STATION_MASTER' WHERE username = 'stationmaster';
UPDATE passenger SET role = 'TICKET_OFFICER' WHERE username = 'ticketofficer';
UPDATE passenger SET role = 'PASSENGER_EXPERIENCE_ANALYST' WHERE username = 'analyst';
UPDATE passenger SET role = 'PASSENGER' WHERE username IN ('john', 'sarah', 'test');

-- Verify the updates
SELECT username, full_name, role FROM passenger ORDER BY id;
