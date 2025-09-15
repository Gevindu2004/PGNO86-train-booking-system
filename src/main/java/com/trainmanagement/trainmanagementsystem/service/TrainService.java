package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.dto.SearchRequest;
import com.trainmanagement.trainmanagementsystem.entity.Schedule;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.repository.ScheduleRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TrainService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    public List<Schedule> searchTrains(SearchRequest request) {
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        LocalDate date = request.getDate();
        LocalTime time = request.getTime();

        // Require from/to; if either is blank, return empty results rather than all schedules
        if (fromStation.isEmpty() || toStation.isEmpty()) {
            return List.of();
        }

        return scheduleRepository.findAll().stream()
                .filter(s -> s.getFromStation() != null && s.getFromStation().equalsIgnoreCase(fromStation))
                .filter(s -> s.getToStation() != null && s.getToStation().equalsIgnoreCase(toStation))
                // Apply date filter only when provided
                .filter(s -> date == null || Objects.equals(s.getDate(), date))
                // Apply time window only when provided (±1 hour)
                .filter(s -> {
                    if (time == null) return true;
                    LocalTime windowStart = time.minusHours(1);
                    LocalTime windowEnd = time.plusHours(1);
                    LocalTime dep = s.getDepartureTime();
                    return dep != null && !dep.isBefore(windowStart) && !dep.isAfter(windowEnd);
                })
                .sorted((a, b) -> {
                    LocalTime ta = a.getDepartureTime();
                    LocalTime tb = b.getDepartureTime();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1; // nulls last
                    if (tb == null) return -1;
                    return ta.compareTo(tb);
                })
                .collect(Collectors.toList());
    }

    public List<Seat> getAvailableSeats(Long scheduleId) {
        return seatRepository.findAll().stream()
                .filter(seat -> seat.getSchedule().getId().equals(scheduleId))
                .filter(Seat::isAvailable)
                .collect(Collectors.toList());
    }
}