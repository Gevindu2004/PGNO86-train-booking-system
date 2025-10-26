-- INSERT PASSENGERS WITH LOGIN CREDENTIALS
INSERT INTO passenger (id, full_name, email, phone, username, password) VALUES
(1, 'John Smith', 'john.smith@email.com', '+94771234567', 'john', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'), -- password: "password"
(2, 'Sarah Johnson', 'sarah.j@email.com', '+94771234568', 'sarah', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'), -- password: "password"
(3, 'Admin User', 'admin@train.com', '+94771234569', 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'), -- password: "password"
(4, 'Test Passenger', 'test@email.com', '+94771234570', 'test', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi') -- password: "password"
ON DUPLICATE KEY UPDATE 
full_name=VALUES(full_name), 
email=VALUES(email), 
phone=VALUES(phone), 
username=VALUES(username), 
password=VALUES(password);

-- INSERT TRAINS
INSERT INTO train (id, name, route) VALUES
                                        (1, 'Blue Line Express', 'Colombo-Kandy'),
                                        (2, 'Highland Cruiser', 'Kandy-Ella'),
                                        (3, 'Polgahawela Express', 'Polgahawela-Colombo')
ON DUPLICATE KEY UPDATE name=VALUES(name), route=VALUES(route);

-- INSERT SCHEDULES
INSERT INTO schedules (schedule_id, date, departure_time, arrival_time, from_station, to_station, status, train_id) VALUES
                                                                                                               (1, CURDATE(), '09:00:00', '12:00:00', 'Colombo', 'Kandy', 'ON_TIME', 1),
                                                                                                               (2, CURDATE(), '14:30:00', '18:15:00', 'Kandy', 'Ella', 'DELAYED', 2),
                                                                                                               (3, CURDATE(), '14:10:00', '18:15:00', 'Polgahawela', 'Colombo', 'ON_TIME', 3)
ON DUPLICATE KEY UPDATE
                     date=VALUES(date),
                     departure_time=VALUES(departure_time),
                     arrival_time=VALUES(arrival_time),
                     from_station=VALUES(from_station),
                     to_station=VALUES(to_station),
                     status=VALUES(status),
                     train_id=VALUES(train_id);

-- INSERT SEATS FOR ALL SCHEDULES
-- Insert seats for Schedule 1 (Train 1) - A1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('A1-W', seat_num), 'A1', b'1', 1, 1
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 1 (Train 1) - B1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('B1-W', seat_num), 'B1', b'1', 1, 1
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 1 (Train 1) - C1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('C1-W', seat_num), 'C1', b'1', 1, 1
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 2 (Train 2) - A1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('A1-W', seat_num), 'A1', b'1', 2, 2
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 2 (Train 2) - B1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('B1-W', seat_num), 'B1', b'1', 2, 2
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 3 (Train 3) - A1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('A1-W', seat_num), 'A1', b'1', 3, 3
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;

-- Insert seats for Schedule 3 (Train 3) - B1 Coach
INSERT IGNORE INTO seat (seat_number, coach_num, available, train_id, schedule_id)
SELECT CONCAT('B1-W', seat_num), 'B1', b'1', 3, 3
FROM (
         SELECT 1 AS seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
         UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
         UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
         UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
     ) AS seat_numbers
ORDER BY seat_num;