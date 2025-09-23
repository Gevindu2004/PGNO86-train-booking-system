
package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPassengerName(String passengerName);

    List<Booking> findByScheduleId(Long scheduleId);
}