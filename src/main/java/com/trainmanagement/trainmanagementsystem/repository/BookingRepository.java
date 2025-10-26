
package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPassengerName(String passengerName);

    List<Booking> findByPassengerNameOrderByBookingTimeDesc(String passengerName);

    @Query("SELECT b FROM Booking b WHERE b.schedule.scheduleId = :scheduleId")
    List<Booking> findByScheduleScheduleId(@Param("scheduleId") Long scheduleId);
    
    // Date-based search methods
    List<Booking> findByBookingTimeBetweenOrderByBookingTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    List<Booking> findByPassengerNameAndBookingTimeBetweenOrderByBookingTimeDesc(String passengerName, LocalDateTime startTime, LocalDateTime endTime);
}