-- Create database objects if they do not exist

CREATE TABLE IF NOT EXISTS `train` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `route` VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `schedules` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `date` DATE NOT NULL,
  `departure_time` TIME NOT NULL,
  `arrival_time` TIME NOT NULL,
  `from_station` VARCHAR(255) NOT NULL,
  `to_station` VARCHAR(255) NOT NULL,
  `train_id` BIGINT,
  CONSTRAINT `fk_schedules_train` FOREIGN KEY (`train_id`) REFERENCES `train`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `seat` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `seat_number` VARCHAR(50) NOT NULL,
  `available` BIT(1) DEFAULT b'1',
  `train_id` BIGINT,
  `schedule_id` BIGINT,
  CONSTRAINT `fk_seat_train` FOREIGN KEY (`train_id`) REFERENCES `train`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_seat_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `booking` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `passenger_name` VARCHAR(255) NOT NULL,
  `booking_time` DATETIME,
  `status` VARCHAR(50) DEFAULT 'CONFIRMED',
  `schedule_id` BIGINT,
  CONSTRAINT `fk_booking_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `booking_seats` (
  `booking_id` BIGINT NOT NULL,
  `seat_id` BIGINT NOT NULL,
  PRIMARY KEY (`booking_id`, `seat_id`),
  CONSTRAINT `fk_booking_seats_booking` FOREIGN KEY (`booking_id`) REFERENCES `booking`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_booking_seats_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;


