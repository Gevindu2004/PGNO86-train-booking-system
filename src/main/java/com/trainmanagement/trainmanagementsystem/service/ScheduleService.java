package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.repository.TrainScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    public TrainSchedule findById(Long id) {
        Optional<TrainSchedule> schedule = trainScheduleRepository.findById(id);
        return schedule.orElse(null); // Return null if not found (handle in controller)
    }

    // Additional methods can be added as needed
    public TrainSchedule save(TrainSchedule schedule) {
        return trainScheduleRepository.save(schedule);
    }
}