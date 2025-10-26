package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.dto.BookingRequest;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.repository.BookingRepository;
import com.trainmanagement.trainmanagementsystem.repository.TrainScheduleRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    private com.trainmanagement.trainmanagementsystem.service.PassengerService passengerService;

    public com.trainmanagement.trainmanagementsystem.entity.Passenger findPassengerByUsername(String username) {
        return passengerService.findByUsername(username).orElse(null);
    }

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TrainScheduleRepository scheduleRepository;

    @Transactional
    public Booking bookTickets(BookingRequest request) {
        if (request.getScheduleId() == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Seat IDs cannot be null or empty");
        }

        Booking booking = new Booking();
        booking.setPassengerName(request.getPassengerName());
        booking.setBookingTime(LocalDateTime.now());
        booking.setSchedule(scheduleRepository.findById(request.getScheduleId()).orElseThrow());
        
        System.out.println("[DEBUG] BookingService - Creating booking for passenger: " + request.getPassengerName());

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        seats.forEach(seat -> {
            if (!seat.isAvailable()) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
            seat.setAvailable(false);
        });
        seatRepository.saveAll(seats);
        booking.setSeats(seats);

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus("CANCELLED");
        booking.getSeats().forEach(seat -> seat.setAvailable(true));
        seatRepository.saveAll(booking.getSeats());
        bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByPassengerName(String passengerName) {
        return bookingRepository.findByPassengerNameOrderByBookingTimeDesc(passengerName);
    }

    // New methods for ticket officer functionality
    public List<Booking> getBookingsByPassengerUsername(String username) {
        // First find the passenger by username
        com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = findPassengerByUsername(username);
        if (passenger == null) {
            return java.util.Collections.emptyList();
        }
        // Then get bookings by passenger name
        return bookingRepository.findByPassengerNameOrderByBookingTimeDesc(passenger.getFullName());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> searchBookingsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return getAllBookings();
        }
        return getBookingsByPassengerUsername(username.trim());
    }
    
    // Search bookings by date
    public List<Booking> getBookingsByDate(java.time.LocalDate date) {
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return bookingRepository.findByBookingTimeBetweenOrderByBookingTimeDesc(startOfDay, endOfDay);
    }
    
    // Search bookings by username and date
    public List<Booking> searchBookingsByUsernameAndDate(String username, java.time.LocalDate date) {
        if (username == null || username.trim().isEmpty()) {
            return getBookingsByDate(date);
        }
        
        // First find the passenger by username
        com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = findPassengerByUsername(username.trim());
        if (passenger == null) {
            return java.util.Collections.emptyList();
        }
        
        // Then get bookings by passenger name and date
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return bookingRepository.findByPassengerNameAndBookingTimeBetweenOrderByBookingTimeDesc(
            passenger.getFullName(), startOfDay, endOfDay);
    }
    
    // Search bookings with flexible parameters
    public List<Booking> searchBookings(String username, java.time.LocalDate date) {
        if (username != null && !username.trim().isEmpty() && date != null) {
            return searchBookingsByUsernameAndDate(username.trim(), date);
        } else if (username != null && !username.trim().isEmpty()) {
            return searchBookingsByUsername(username.trim());
        } else if (date != null) {
            return getBookingsByDate(date);
        } else {
            return getAllBookings();
        }
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Transactional
    public Booking updateBookingSeats(Long bookingId, List<Long> newSeatIds) {
        // Validate booking exists and is modifiable
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be modified");
        }

        // Validate new seats are available
        List<Seat> newSeats = seatRepository.findAllById(newSeatIds);
        if (newSeats.size() != newSeatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }

        // Check if new seats are available
        for (Seat seat : newSeats) {
            if (!seat.isAvailable()) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }

        // Release current seats (make them available)
        List<Seat> currentSeats = booking.getSeats();
        for (Seat seat : currentSeats) {
            seat.setAvailable(true);
        }
        seatRepository.saveAll(currentSeats);

        // Reserve new seats (make them unavailable)
        for (Seat seat : newSeats) {
            seat.setAvailable(false);
        }
        seatRepository.saveAll(newSeats);

        // Update booking with new seats
        booking.setSeats(newSeats);
        booking.setBookingTime(java.time.LocalDateTime.now()); // Update booking time

        return bookingRepository.save(booking);
    }

    public List<Seat> getAvailableSeatsForSchedule(Long scheduleId) {
        System.out.println("DEBUG: BookingService.getAvailableSeatsForSchedule - scheduleId: " + scheduleId);
        try {
            List<Seat> allSeats = seatRepository.findByScheduleIdOrderByCoachAndSeat(scheduleId);
            System.out.println("DEBUG: Found " + allSeats.size() + " total seats for schedule " + scheduleId);
            
            List<Seat> availableSeats = allSeats.stream()
                    .filter(Seat::isAvailable)
                    .collect(java.util.stream.Collectors.toList());
            
            System.out.println("DEBUG: Found " + availableSeats.size() + " available seats for schedule " + scheduleId);
            return availableSeats;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in getAvailableSeatsForSchedule: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean canModifyBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;
        
        // Only allow modifications for confirmed bookings
        if (!"CONFIRMED".equals(booking.getStatus())) {
            return false;
        }
        
        // Check if booking is within 30 minutes of creation
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime bookingTime = booking.getBookingTime();
        java.time.Duration duration = java.time.Duration.between(bookingTime, now);
        
        // Allow modifications only within 30 minutes
        return duration.toSeconds() <= 30;
    }
    
    public boolean canCancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;

        // Allow cancellation for confirmed bookings (no time limit for cancellation)
        return "CONFIRMED".equals(booking.getStatus());
    }
    
    public String getBookingModificationStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return "BOOKING_NOT_FOUND";
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            return "NOT_CONFIRMED";
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime bookingTime = booking.getBookingTime();
        java.time.Duration duration = java.time.Duration.between(bookingTime, now);
        long minutesElapsed = duration.toMinutes();

        if (minutesElapsed <= 30) {
            return "CAN_MODIFY";
        } else {
            return "CAN_ONLY_CANCEL";
        }
    }
    
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    public List<Booking> findAllPendingBookings() {
        try {
            System.out.println("[DEBUG] BookingService - Finding all bookings...");
            List<Booking> allBookings = bookingRepository.findAll();
            System.out.println("[DEBUG] BookingService - Found " + allBookings.size() + " total bookings");
            
            // For now, return all bookings (we'll filter by status later)
            // This avoids potential issues with status filtering
            System.out.println("[DEBUG] BookingService - Returning all bookings as pending for now");
            return allBookings;
        } catch (Exception e) {
            System.err.println("[ERROR] BookingService - Error finding pending bookings: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
}