package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {}
