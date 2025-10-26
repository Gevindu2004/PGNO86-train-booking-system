-- Script to populate seat table with data for all schedules
-- This script will add seats for all schedules with the new structure:
-- 5 coaches in Class A (A1-A5), 5 coaches in Class B (B1-B5), 5 coaches in Class C (C1-C5)
-- Each coach has 20 seats, total 300 seats per schedule

-- Function to generate seats for all coaches (A1-A5, B1-B5, C1-C5) for a given schedule
-- Usage: CALL GenerateSeatsForSchedule(train_id, schedule_id);

DELIMITER //

CREATE PROCEDURE GenerateSeatsForSchedule(IN p_train_id INT, IN p_schedule_id INT)
BEGIN
    DECLARE coach_prefix VARCHAR(1);
    DECLARE coach_num INT;
    DECLARE seat_num INT;
    DECLARE coach_name VARCHAR(3);
    
    -- Check if seats already exist for this schedule
    IF (SELECT COUNT(*) FROM seat WHERE schedule_id = p_schedule_id) = 0 THEN
        
        -- Generate seats for Class A (A1, A2, A3, A4, A5)
        SET coach_prefix = 'A';
        SET coach_num = 1;
        WHILE coach_num <= 5 DO
            SET coach_name = CONCAT(coach_prefix, coach_num);
            SET seat_num = 1;
            WHILE seat_num <= 20 DO
                INSERT INTO seat (seat_number, coach_num, available, train_id, schedule_id)
                VALUES (CONCAT(coach_name, '-W', seat_num), coach_name, b'1', p_train_id, p_schedule_id);
                SET seat_num = seat_num + 1;
            END WHILE;
            SET coach_num = coach_num + 1;
        END WHILE;
        
        -- Generate seats for Class B (B1, B2, B3, B4, B5)
        SET coach_prefix = 'B';
        SET coach_num = 1;
        WHILE coach_num <= 5 DO
            SET coach_name = CONCAT(coach_prefix, coach_num);
            SET seat_num = 1;
            WHILE seat_num <= 20 DO
                INSERT INTO seat (seat_number, coach_num, available, train_id, schedule_id)
                VALUES (CONCAT(coach_name, '-W', seat_num), coach_name, b'1', p_train_id, p_schedule_id);
                SET seat_num = seat_num + 1;
            END WHILE;
            SET coach_num = coach_num + 1;
        END WHILE;
        
        -- Generate seats for Class C (C1, C2, C3, C4, C5)
        SET coach_prefix = 'C';
        SET coach_num = 1;
        WHILE coach_num <= 5 DO
            SET coach_name = CONCAT(coach_prefix, coach_num);
            SET seat_num = 1;
            WHILE seat_num <= 20 DO
                INSERT INTO seat (seat_number, coach_num, available, train_id, schedule_id)
                VALUES (CONCAT(coach_name, '-W', seat_num), coach_name, b'1', p_train_id, p_schedule_id);
                SET seat_num = seat_num + 1;
            END WHILE;
            SET coach_num = coach_num + 1;
        END WHILE;
        
    END IF;
END //

DELIMITER ;

-- Generate seats for existing schedules
-- Replace the train_id and schedule_id values with actual values from your database

-- Example: Generate seats for Schedule 1 (Train 1)
CALL GenerateSeatsForSchedule(1, 1);

-- Example: Generate seats for Schedule 2 (Train 2)
CALL GenerateSeatsForSchedule(2, 2);

-- Example: Generate seats for Schedule 3 (Train 3)
CALL GenerateSeatsForSchedule(3, 3);

-- Add more calls as needed for other schedules...

-- Verify the data was inserted correctly
SELECT 
    schedule_id,
    coach_num,
    COUNT(*) as seat_count,
    SUM(CASE WHEN available = 1 THEN 1 ELSE 0 END) as available_seats
FROM seat 
GROUP BY schedule_id, coach_num
ORDER BY schedule_id, coach_num;

-- Summary by schedule
SELECT 
    schedule_id,
    COUNT(*) as total_seats,
    SUM(CASE WHEN available = 1 THEN 1 ELSE 0 END) as available_seats,
    COUNT(DISTINCT coach_num) as total_coaches
FROM seat 
GROUP BY schedule_id
ORDER BY schedule_id;

-- Drop the procedure after use
DROP PROCEDURE IF EXISTS GenerateSeatsForSchedule;
