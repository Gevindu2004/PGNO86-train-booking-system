package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.service.TrainScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleRestController {

    @Autowired
    private TrainScheduleService trainScheduleService;

    @GetMapping
    public ResponseEntity<List<TrainSchedule>> getAllSchedules() {
        List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainSchedule> getScheduleById(@PathVariable Long id) {
        List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
        TrainSchedule schedule = schedules.stream()
                .filter(s -> s.getScheduleId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (schedule != null) {
            return ResponseEntity.ok(schedule);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TrainSchedule> createSchedule(@RequestBody TrainSchedule schedule) {
        try {
            TrainSchedule created = trainScheduleService.createSchedule(schedule);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainSchedule> updateSchedule(@PathVariable Long id, @RequestBody TrainSchedule schedule) {
        try {
            TrainSchedule updated = trainScheduleService.updateSchedule(id, schedule);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        try {
            trainScheduleService.deleteSchedule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrainSchedule>> getSchedulesByStatus(@PathVariable String status) {
        List<TrainSchedule> allSchedules = trainScheduleService.getAllSchedules();
        List<TrainSchedule> filteredSchedules = allSchedules.stream()
                .filter(s -> status.equals(s.getStatusString()))
                .toList();
        return ResponseEntity.ok(filteredSchedules);
    }
}
