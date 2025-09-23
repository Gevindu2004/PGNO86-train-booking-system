package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.Schedule;
import com.trainmanagement.trainmanagementsystem.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Schedule findById(Long id) {
        Optional<Schedule> schedule = scheduleRepository.findById(id);
        return schedule.orElse(null); // Return null if not found (handle in controller)
    }

    // Additional methods can be added as needed
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }
}