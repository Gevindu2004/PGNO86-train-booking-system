package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.repository.TrainScheduleRepository;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainScheduleService {

    private final TrainScheduleRepository repository;

    public TrainScheduleService(TrainScheduleRepository repository) {
        this.repository = repository;
    }

    public List<TrainSchedule> getAllSchedules() {
        return repository.findAll();
    }

    public TrainSchedule createSchedule(TrainSchedule schedule) {
        // Set default values
        if (schedule.getDate() == null) {
            schedule.setDate(LocalDate.now());
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(TrainSchedule.ScheduleStatus.ON_TIME);
        }
        TrainSchedule saved = repository.save(schedule);
        return saved;
    }

    public TrainSchedule updateSchedule(Long id, TrainSchedule updatedSchedule) {
        return repository.findById(id).map(existing -> {
            existing.setDate(updatedSchedule.getDate());
            existing.setDepartureTime(updatedSchedule.getDepartureTime());
            existing.setArrivalTime(updatedSchedule.getArrivalTime());
            existing.setFromStation(updatedSchedule.getFromStation());
            existing.setToStation(updatedSchedule.getToStation());
            existing.setStatus(updatedSchedule.getStatus());
            
            // Handle train relationship properly
            if (updatedSchedule.getTrain() != null) {
                existing.setTrain(updatedSchedule.getTrain());
            } else if (updatedSchedule.getTrainId() != null) {
                existing.setTrainId(updatedSchedule.getTrainId());
            }
            
            TrainSchedule saved = repository.save(existing);
            return saved;
        }).orElse(null);
    }

    public void deleteSchedule(Long id) {
        repository.findById(id).ifPresent(existing -> {
            repository.deleteById(id);
        });
    }
}
