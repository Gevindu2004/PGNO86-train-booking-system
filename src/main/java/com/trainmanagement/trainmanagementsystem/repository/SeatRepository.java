package com.trainmanagement.trainmanagementsystem.repository;


import com.trainmanagement.trainmanagementsystem.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScheduleIdAndAvailableTrue(Long scheduleId);
    List<Seat> findAllByIdIn(List<Long> seatIds);
}