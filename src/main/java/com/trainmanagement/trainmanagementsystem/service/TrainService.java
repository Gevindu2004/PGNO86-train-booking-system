package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.dto.SearchRequest;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.repository.TrainScheduleRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TrainService {

    @Autowired
    private TrainScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    public List<TrainSchedule> searchTrains(SearchRequest request) {
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        LocalDate date = request.getDate();
        LocalTime time = request.getTime();

        if (fromStation.isEmpty() || toStation.isEmpty()) {
            return List.of();
        }

        return scheduleRepository.findAll().stream()
                .filter(s -> s.getFromStation() != null && s.getFromStation().equalsIgnoreCase(fromStation))
                .filter(s -> s.getToStation() != null && s.getToStation().equalsIgnoreCase(toStation))
                .filter(s -> date == null || Objects.equals(s.getDate(), date))
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
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return ta.compareTo(tb);
                })
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "seats", allEntries = true)
    public List<Seat> getSeatsForSchedule(Long scheduleId) {
        List<Seat> seats = seatRepository.findByScheduleIdAndCoachNumOrderBySeatNumberAsc(scheduleId, null);
        return seats.stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    if (a == null || b == null) return 0;
                    String coachA = a.getCoachNum() != null ? a.getCoachNum() : "";
                    String coachB = b.getCoachNum() != null ? b.getCoachNum() : "";
                    String seatA = a.getSeatNumber() != null ? a.getSeatNumber() : "";
                    String seatB = b.getSeatNumber() != null ? b.getSeatNumber() : "";

                    // Sort by coach class (A, B, C) first
                    char classA = coachA.isEmpty() ? 'Z' : Character.toUpperCase(coachA.charAt(0));
                    char classB = coachB.isEmpty() ? 'Z' : Character.toUpperCase(coachB.charAt(0));
                    if (classA != classB) return Character.compare(classA, classB);

                    // Then by coach number (e.g., 1 in A1)
                    int coachIndexA = extractCoachIndex(coachA);
                    int coachIndexB = extractCoachIndex(coachB);
                    if (coachIndexA != coachIndexB) return Integer.compare(coachIndexA, coachIndexB);

                    // Then by seat number (e.g., 1 in W1)
                    int seatIndexA = extractSeatIndex(seatA);
                    int seatIndexB = extractSeatIndex(seatB);
                    return Integer.compare(seatIndexA, seatIndexB);
                })
                .collect(Collectors.toList());
    }

    public List<Seat> getSeatsForScheduleAndCoach(Long scheduleId, String coachNum) {
        return seatRepository.findByScheduleIdAndCoachNumOrderBySeatNumberAsc(scheduleId, coachNum)
                .stream()
                .filter(Objects::nonNull)
                .filter(Seat::isAvailable)
                .sorted((a, b) -> {
                    if (a == null || b == null) return 0;
                    String seatA = a.getSeatNumber() != null ? a.getSeatNumber() : "";
                    String seatB = b.getSeatNumber() != null ? b.getSeatNumber() : "";
                    int seatIndexA = extractSeatIndex(seatA);
                    int seatIndexB = extractSeatIndex(seatB);
                    return Integer.compare(seatIndexA, seatIndexB);
                })
                .collect(Collectors.toList());
    }

    private int extractCoachIndex(String coachNum) {
        if (coachNum.isEmpty()) return 0;
        String numStr = coachNum.replaceAll("[^0-9]", "");
        return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
    }

    private int extractSeatIndex(String seatNum) {
        if (seatNum.isEmpty()) return 0;
        String afterHyphen = seatNum.substring(seatNum.lastIndexOf('-') + 1).replaceAll("[^0-9]", "");
        return afterHyphen.isEmpty() ? 0 : Integer.parseInt(afterHyphen);
    }
}