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

    public interface Search {
        List<TrainSchedule> execute(List<TrainSchedule> schedules, SearchRequest request);
        String getStrategyName();
    }
    

    public class StationSearch implements Search {
        @Override
        public List<TrainSchedule> execute(List<TrainSchedule> schedules, SearchRequest request) {
            String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
            String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
            
            if (fromStation.isEmpty() || toStation.isEmpty()) {
                return schedules;
            }
            
            return schedules.stream()
                    .filter(s -> s.getFromStation() != null && s.getFromStation().equalsIgnoreCase(fromStation))
                    .filter(s -> s.getToStation() != null && s.getToStation().equalsIgnoreCase(toStation))
                    .collect(Collectors.toList());
        }
        
        @Override
        public String getStrategyName() {
            return "Station Search";
        }
    }
    

    public class DateSearch implements Search {
        @Override
        public List<TrainSchedule> execute(List<TrainSchedule> schedules, SearchRequest request) {
            // Enforce station requirement
            String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
            String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
            
            if (fromStation.isEmpty() || toStation.isEmpty()) {
                return List.of(); // Return empty if stations not provided
            }
            
            LocalDate date = request.getDate();
            if (date == null) {
                return schedules;
            }
            
            return schedules.stream()
                    .filter(s -> s.getFromStation() != null && s.getFromStation().equalsIgnoreCase(fromStation))
                    .filter(s -> s.getToStation() != null && s.getToStation().equalsIgnoreCase(toStation))
                    .filter(s -> Objects.equals(s.getDate(), date))
                    .collect(Collectors.toList());
        }
        
        @Override
        public String getStrategyName() {
            return "Date Search";
        }
    }

    public class TimeWindowSearch implements Search {
        @Override
        public List<TrainSchedule> execute(List<TrainSchedule> schedules, SearchRequest request) {
            // Enforce station requirement
            String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
            String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
            
            if (fromStation.isEmpty() || toStation.isEmpty()) {
                return List.of();
            }
            
            LocalTime time = request.getTime();
            if (time == null) {
                return schedules;
            }
            
            LocalTime windowStart = time.minusHours(1);
            LocalTime windowEnd = time.plusHours(1);
            
            return schedules.stream()
                    .filter(s -> s.getFromStation() != null && s.getFromStation().equalsIgnoreCase(fromStation))
                    .filter(s -> s.getToStation() != null && s.getToStation().equalsIgnoreCase(toStation))
                    .filter(s -> {
                        LocalTime dep = s.getDepartureTime();
                        return dep != null && !dep.isBefore(windowStart) && !dep.isAfter(windowEnd);
                    })
                    .collect(Collectors.toList());
        }
        
        @Override
        public String getStrategyName() {
            return "Time Window Search";
        }
    }
    

    public class ComprehensiveSearch implements Search {
        @Override
        public List<TrainSchedule> execute(List<TrainSchedule> schedules, SearchRequest request) {
            String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
            String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
            LocalDate date = request.getDate();
            LocalTime time = request.getTime();

            if (fromStation.isEmpty() || toStation.isEmpty()) {
                return List.of();
            }

            return schedules.stream()
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
        
        @Override
        public String getStrategyName() {
            return "Comprehensive Search";
        }
    }

    public class SearchContext {
        private Search currentStrategy;
        
        public void setStrategy(Search strategy) {
            this.currentStrategy = strategy;
        }
        
        public List<TrainSchedule> executeSearch(List<TrainSchedule> schedules, SearchRequest request) {
            if (currentStrategy == null) {
                throw new IllegalStateException("No strategy has been set");
            }
            return currentStrategy.execute(schedules, request);
        }
        
        public String getCurrentStrategyName() {
            return currentStrategy != null ? currentStrategy.getStrategyName() : "None";
        }
        
        public void autoSelectStrategy(SearchRequest request) {

            String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
            String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
            

            if (fromStation.isEmpty() || toStation.isEmpty()) {
                setStrategy(new ComprehensiveSearch());
                return;
            }
            
            int criteriaCount = 2;
            
            if (request.getDate() != null) criteriaCount++;
            if (request.getTime() != null) criteriaCount++;
            
            // Select strategy based on additional criteria beyond stations
            if (criteriaCount >= 4) {
                setStrategy(new ComprehensiveSearch()); // Stations + Date + Time
            } else if (criteriaCount == 3) {
                if (request.getDate() != null) {
                    setStrategy(new DateSearch()); // Stations + Date
                } else if (request.getTime() != null) {
                    setStrategy(new TimeWindowSearch()); // Stations + Time
                } else {
                    setStrategy(new ComprehensiveSearch());
                }
            } else {
                setStrategy(new StationSearch()); // Only stations
            }
        }
    }
    
    // Strategy context instance
    private final SearchContext searchContext = new SearchContext();

    private List<TrainSchedule> sortSchedulesByDepartureTime(List<TrainSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return schedules;
        }
        
        return schedules.stream()
                .filter(schedule -> schedule.getDepartureTime() != null)
                .sorted((a, b) -> {
                    LocalTime timeA = a.getDepartureTime();
                    LocalTime timeB = b.getDepartureTime();

                    if (timeA == null && timeB == null) return 0;
                    if (timeA == null) return 1;
                    if (timeB == null) return -1;
                    
                    return timeA.compareTo(timeB);
                })
                .collect(Collectors.toList());
    }

    /**
     * Search trains using Strategy pattern
     * This method demonstrates the Strategy pattern implementation
     * Enforces that both from and to stations are required
     */
    public List<TrainSchedule> searchTrains(SearchRequest request) {
        // Validate station requirement
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        
        if (fromStation.isEmpty() || toStation.isEmpty()) {
            System.out.println("Search failed: Both from and to stations are required");
            return List.of(); // Return empty list if stations not provided
        }
        
        // Auto-select the best strategy based on criteria
        searchContext.autoSelectStrategy(request);
        
        // Get all schedules from repository
        List<TrainSchedule> allSchedules = scheduleRepository.findAll();
        
        // Execute search using the selected strategy
        List<TrainSchedule> results = searchContext.executeSearch(allSchedules, request);
        
        // Sort results by departure time (earliest first)
        results = sortSchedulesByDepartureTime(results);
        
        System.out.println("Using strategy: " + searchContext.getCurrentStrategyName());
        System.out.println("Found " + results.size() + " trains matching criteria");
        
        return results;
    }
    
    /**
     * Search trains with a specific strategy
     * This method allows manual strategy selection
     * Enforces that both from and to stations are required
     */
    public List<TrainSchedule> searchTrainsWithStrategy(SearchRequest request, String strategyName) {
        // Validate station requirement
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        
        if (fromStation.isEmpty() || toStation.isEmpty()) {
            System.out.println("Search failed: Both from and to stations are required");
            return List.of(); // Return empty list if stations not provided
        }
        
        // Set specific strategy
        switch (strategyName) {
            case "Station Search":
                searchContext.setStrategy(new StationSearch());
                break;
            case "Date Search":
                searchContext.setStrategy(new DateSearch());
                break;
            case "Time Window Search":
                searchContext.setStrategy(new TimeWindowSearch());
                break;
            case "Comprehensive Search":
                searchContext.setStrategy(new ComprehensiveSearch());
                break;
            default:
                searchContext.setStrategy(new ComprehensiveSearch());
        }
        
        // Get all schedules from repository
        List<TrainSchedule> allSchedules = scheduleRepository.findAll();
        
        // Execute search using the selected strategy
        List<TrainSchedule> results = searchContext.executeSearch(allSchedules, request);
        
        // Sort results by departure time (earliest first)
        results = sortSchedulesByDepartureTime(results);
        
        System.out.println("Using strategy: " + searchContext.getCurrentStrategyName());
        System.out.println("Found " + results.size() + " trains matching criteria");
        
        return results;
    }
    
    /**
     * Get available search strategies
     */
    public List<String> getAvailableSearchStrategies() {
        return List.of("Station Search", "Date Search", "Time Window Search", "Comprehensive Search");
    }
    
    /**
     * Legacy search method (kept for backward compatibility)
     * This method shows the old approach without Strategy pattern
     */
    public List<TrainSchedule> searchTrainsLegacy(SearchRequest request) {
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