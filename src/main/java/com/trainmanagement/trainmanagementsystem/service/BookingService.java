package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.dto.BookingRequest;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.repository.BookingRepository;
import com.trainmanagement.trainmanagementsystem.repository.ScheduleRepository;
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
    private ScheduleRepository scheduleRepository;

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
}