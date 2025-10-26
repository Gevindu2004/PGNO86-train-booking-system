package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    List<TrainSchedule> findByFromStationAndToStationAndDate(String fromStation, String toStation, LocalDate date);
    List<TrainSchedule> findByDateBetween(LocalDate startDate, LocalDate endDate);
}