package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.schedule.scheduleId = :scheduleId AND s.available = true")
    List<Seat> findByScheduleIdAndAvailableTrue(@Param("scheduleId") Long scheduleId);

    @Query("SELECT s FROM Seat s WHERE s.schedule.scheduleId = :scheduleId AND (:coachNum IS NULL OR s.coachNum = :coachNum) ORDER BY s.seatNumber ASC")
    List<Seat> findByScheduleIdAndCoachNumOrderBySeatNumberAsc(@Param("scheduleId") Long scheduleId, @Param("coachNum") String coachNum);
    
    @Query("SELECT s FROM Seat s WHERE s.schedule.scheduleId = :scheduleId ORDER BY s.coachNum ASC, s.seatNumber ASC")
    List<Seat> findByScheduleIdOrderByCoachAndSeat(@Param("scheduleId") Long scheduleId);

    @Query("SELECT s FROM Seat s WHERE s.schedule IS NULL")
    List<Seat> findByScheduleIdIsNull();

    List<Seat> findAllByIdIn(List<Long> seatIds);
}