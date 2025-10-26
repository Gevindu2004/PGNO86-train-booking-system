-- Sample pricing data for testing the ticket officer pricing management system
-- This file contains additional pricing records for various routes and classes

-- Additional routes for testing
INSERT IGNORE INTO pricing (from_station, to_station, class_type, price) VALUES
-- Colombo to other destinations
('Colombo', 'Galle', 'A', 1500.00),
('Colombo', 'Galle', 'B', 400.00),
('Colombo', 'Galle', 'C', 200.00),

('Colombo', 'Anuradhapura', 'A', 2500.00),
('Colombo', 'Anuradhapura', 'B', 600.00),
('Colombo', 'Anuradhapura', 'C', 300.00),

('Colombo', 'Jaffna', 'A', 3500.00),
('Colombo', 'Jaffna', 'B', 800.00),
('Colombo', 'Jaffna', 'C', 400.00),

-- Kandy to other destinations
('Kandy', 'Nuwara Eliya', 'A', 1200.00),
('Kandy', 'Nuwara Eliya', 'B', 350.00),
('Kandy', 'Nuwara Eliya', 'C', 180.00),

('Kandy', 'Badulla', 'A', 1800.00),
('Kandy', 'Badulla', 'B', 500.00),
('Kandy', 'Badulla', 'C', 250.00),

-- Galle to other destinations
('Galle', 'Matara', 'A', 800.00),
('Galle', 'Matara', 'B', 250.00),
('Galle', 'Matara', 'C', 120.00),

('Galle', 'Hambantota', 'A', 1200.00),
('Galle', 'Hambantota', 'B', 350.00),
('Galle', 'Hambantota', 'C', 180.00),

-- Reverse routes
('Galle', 'Colombo', 'A', 1500.00),
('Galle', 'Colombo', 'B', 400.00),
('Galle', 'Colombo', 'C', 200.00),

('Anuradhapura', 'Colombo', 'A', 2500.00),
('Anuradhapura', 'Colombo', 'B', 600.00),
('Anuradhapura', 'Colombo', 'C', 300.00),

('Jaffna', 'Colombo', 'A', 3500.00),
('Jaffna', 'Colombo', 'B', 800.00),
('Jaffna', 'Colombo', 'C', 400.00),

('Nuwara Eliya', 'Kandy', 'A', 1200.00),
('Nuwara Eliya', 'Kandy', 'B', 350.00),
('Nuwara Eliya', 'Kandy', 'C', 180.00),

('Badulla', 'Kandy', 'A', 1800.00),
('Badulla', 'Kandy', 'B', 500.00),
('Badulla', 'Kandy', 'C', 250.00),

('Matara', 'Galle', 'A', 800.00),
('Matara', 'Galle', 'B', 250.00),
('Matara', 'Galle', 'C', 120.00),

('Hambantota', 'Galle', 'A', 1200.00),
('Hambantota', 'Galle', 'B', 350.00),
('Hambantota', 'Galle', 'C', 180.00);

-- Verify the data
SELECT COUNT(*) as total_pricing_records FROM pricing;
SELECT from_station, to_station, class_type, price FROM pricing ORDER BY from_station, to_station, class_type;
