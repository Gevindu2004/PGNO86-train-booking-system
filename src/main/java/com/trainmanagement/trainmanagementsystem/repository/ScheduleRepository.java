package com.trainmanagement.trainmanagementsystem.repository;



import com.trainmanagement.trainmanagementsystem.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByFromStationAndToStationAndDate(String fromStation, String toStation, LocalDate date);
    List<Schedule> findByDateBetween(LocalDate startDate, LocalDate endDate);
}