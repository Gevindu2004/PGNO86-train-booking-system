-- TRAINS
INSERT INTO train (id, name, route) VALUES (1, 'Blue Line Express', 'Colombo-Kandy') ON DUPLICATE KEY UPDATE name=VALUES(name), route=VALUES(route);
INSERT INTO train (id, name, route) VALUES (2, 'Highland Cruiser', 'Kandy-Ella') ON DUPLICATE KEY UPDATE name=VALUES(name), route=VALUES(route);

-- SCHEDULES
INSERT INTO schedules (id, date, departure_time, arrival_time, from_station, to_station, train_id)
VALUES (1, CURDATE(), '09:00:00', '12:00:00', 'Colombo', 'Kandy', 1)
ON DUPLICATE KEY UPDATE date=VALUES(date), departure_time=VALUES(departure_time), arrival_time=VALUES(arrival_time), from_station=VALUES(from_station), to_station=VALUES(to_station), train_id=VALUES(train_id);

INSERT INTO schedules (id, date, departure_time, arrival_time, from_station, to_station, train_id)
VALUES (2, CURDATE(), '14:30:00', '18:15:00', 'Kandy', 'Ella', 2)
ON DUPLICATE KEY UPDATE date=VALUES(date), departure_time=VALUES(departure_time), arrival_time=VALUES(arrival_time), from_station=VALUES(from_station), to_station=VALUES(to_station), train_id=VALUES(train_id);

-- SEATS (for schedule 1)
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (1, 'A1', true, 1, 1)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (2, 'A2', true, 1, 1)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (3, 'A3', true, 1, 1)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);

-- SEATS (for schedule 2)
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (4, 'B1', true, 2, 2)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (5, 'B2', true, 2, 2)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);
INSERT INTO seat (id, seat_number, available, train_id, schedule_id) VALUES (6, 'B3', true, 2, 2)
ON DUPLICATE KEY UPDATE available=VALUES(available), train_id=VALUES(train_id), schedule_id=VALUES(schedule_id);


