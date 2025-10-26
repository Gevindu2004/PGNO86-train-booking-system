-- TRAIN TABLE
CREATE TABLE IF NOT EXISTS `train` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `route` VARCHAR(255)
) ENGINE=InnoDB;

-- SCHEDULE TABLE
CREATE TABLE IF NOT EXISTS `schedules` (
    `schedule_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `departure_time` TIME NOT NULL,
    `arrival_time` TIME NOT NULL,
    `from_station` VARCHAR(255) NOT NULL,
    `to_station` VARCHAR(255) NOT NULL,
    `status` ENUM('ON_TIME', 'DELAYED', 'CANCELLED') DEFAULT 'ON_TIME',
    `train_id` BIGINT,
    CONSTRAINT `fk_schedules_train` FOREIGN KEY (`train_id`)
        REFERENCES `train`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- SEAT TABLE
CREATE TABLE IF NOT EXISTS `seat` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `seat_number` VARCHAR(50) NOT NULL,
    `coach_num` VARCHAR(3) NOT NULL DEFAULT 'A1',
    `available` BIT(1) DEFAULT b'1',
    `train_id` BIGINT,
    `schedule_id` BIGINT,
    CONSTRAINT `coach_chk_1` CHECK (
        `coach_num` IN (
            'A1','A2','A3','A4','A5',
            'B1','B2','B3','B4','B5',
            'C1','C2','C3','C4','C5'
        )
    ),
    CONSTRAINT `fk_seat_train` FOREIGN KEY (`train_id`)
        REFERENCES `train`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_seat_schedule` FOREIGN KEY (`schedule_id`)
        REFERENCES `schedules`(`schedule_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- BOOKING TABLE
CREATE TABLE IF NOT EXISTS `booking` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `passenger_name` VARCHAR(255) NOT NULL,
    `booking_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('CONFIRMED', 'CANCELLED', 'PENDING') DEFAULT 'CONFIRMED',
    `schedule_id` BIGINT,
    CONSTRAINT `fk_booking_schedule` FOREIGN KEY (`schedule_id`)
        REFERENCES `schedules`(`schedule_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- PASSENGER TABLE
CREATE TABLE IF NOT EXISTS `passenger` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `full_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `phone` VARCHAR(20),
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` ENUM('PASSENGER', 'TRAIN_STATION_MASTER', 'TICKET_OFFICER', 'PASSENGER_EXPERIENCE_ANALYST', 'ADMIN_STAFF') DEFAULT 'PASSENGER',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- BOOKING_SEATS TABLE
CREATE TABLE IF NOT EXISTS `booking_seats` (
    `booking_id` BIGINT NOT NULL,
    `seat_id` BIGINT NOT NULL,
    PRIMARY KEY (`booking_id`, `seat_id`),
    CONSTRAINT `fk_booking_seats_booking` FOREIGN KEY (`booking_id`)
        REFERENCES `booking`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_booking_seats_seat` FOREIGN KEY (`seat_id`)
        REFERENCES `seat`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- PRICING TABLE
CREATE TABLE IF NOT EXISTS `pricing` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `from_station` VARCHAR(255) NOT NULL,
    `to_station` VARCHAR(255) NOT NULL,
    `class_type` ENUM('A', 'B', 'C') NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `unique_route_class` (`from_station`, `to_station`, `class_type`)
) ENGINE=InnoDB;

-- PASSENGER FEEDBACK TABLE
CREATE TABLE IF NOT EXISTS `passenger_feedback` (
    `feedback_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `passenger_id` BIGINT NOT NULL,
    `ticket_id` BIGINT,
    `message` LONGTEXT,
    `response` LONGTEXT,
    `status` VARCHAR(50) DEFAULT 'New',
    `submitted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` BIGINT,
    CONSTRAINT `fk_feedback_passenger` FOREIGN KEY (`passenger_id`)
        REFERENCES `passenger`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_feedback_booking` FOREIGN KEY (`ticket_id`)
        REFERENCES `booking`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_feedback_updated_by` FOREIGN KEY (`updated_by`)
        REFERENCES `passenger`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ALERTS TABLE
CREATE TABLE IF NOT EXISTS `alerts` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `message` LONGTEXT NOT NULL,
    `posted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `posted_by` BIGINT NOT NULL,
    CONSTRAINT `fk_alerts_posted_by` FOREIGN KEY (`posted_by`)
        REFERENCES `passenger`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;