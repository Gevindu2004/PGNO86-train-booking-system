package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScheduleIdAndAvailableTrue(Long scheduleId);

    @Query("SELECT s FROM Seat s WHERE s.schedule.id = :scheduleId AND (:coachNum IS NULL OR s.coachNum = :coachNum) ORDER BY s.seatNumber ASC")
    List<Seat> findByScheduleIdAndCoachNumOrderBySeatNumberAsc(@Param("scheduleId") Long scheduleId, @Param("coachNum") String coachNum);

    List<Seat> findAllByIdIn(List<Long> seatIds);
}