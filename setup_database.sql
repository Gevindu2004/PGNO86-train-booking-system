-- Manual database setup script
-- Run this in your MySQL database to ensure passenger table exists

USE `train-management-system`;

-- Create passenger table
CREATE TABLE IF NOT EXISTS `passenger` (
    `scheduleId` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `full_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `phone` VARCHAR(20),
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Insert test passengers
INSERT IGNORE INTO passenger (scheduleId, full_name, email, phone, username, password) VALUES
(1, 'John Smith', 'john.smith@email.com', '+94771234567', 'john', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
(2, 'Sarah Johnson', 'sarah.j@email.com', '+94771234568', 'sarah', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
(3, 'Admin User', 'admin@train.com', '+94771234569', 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'),
(4, 'Test Passenger', 'test@email.com', '+94771234570', 'test', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Verify the table was created
SELECT 'Passenger table created successfully' as status;
SELECT COUNT(*) as passenger_count FROM passenger;
