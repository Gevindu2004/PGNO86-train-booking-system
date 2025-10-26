package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.dto.SearchRequest;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import com.trainmanagement.trainmanagementsystem.service.ScheduleService;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/trains")
public class TrainController {
    @Autowired
    private TrainService trainService;

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private PassengerService passengerService;
    
    @Autowired
    private RoleBasedAccessControl rbac;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        // Ensure form-backing bean exists
        model.addAttribute("searchRequest", new SearchRequest());
        
        // Add authentication information for navigation bar
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "search";  // Renders search.html
    }

    // Optional: root redirect to search page
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/trains/search";
    }

    @PostMapping("/search")
    public String searchTrains(@ModelAttribute SearchRequest request, Model model) {
        // Validate station requirement
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        
        if (fromStation.isEmpty() || toStation.isEmpty()) {
            model.addAttribute("error", "Both 'From' and 'To' stations are required for train search.");
            model.addAttribute("searchRequest", request);
            
            // Add authentication information
            try {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof String && !"anonymousUser".equals(principal)) {
                    String username = (String) principal;
                    Passenger passenger = passengerService.findByUsername(username).orElse(null);
                    if (passenger != null) {
                        model.addAttribute("currentUser", passenger);
                        model.addAttribute("userRole", passenger.getRole());
                        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                        model.addAttribute("isAuthenticated", true);
                        model.addAttribute("username", passenger.getUsername());
                    } else {
                        model.addAttribute("isAuthenticated", false);
                    }
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } catch (Exception e) {
                model.addAttribute("isAuthenticated", false);
            }
            
            return "search";
        }
        
        // Use Strategy pattern for search
        List<TrainSchedule> results = trainService.searchTrains(request);
        model.addAttribute("results", results);
        model.addAttribute("searchRequest", request);
        
        // Add authentication information for navigation bar
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "search";  // Show results in same page
    }
    
    /**
     * Search with specific strategy - demonstrates Strategy pattern usage
     */
    @PostMapping("/search/strategy/{strategyName}")
    public String searchTrainsWithStrategy(@ModelAttribute SearchRequest request, 
                                         @PathVariable String strategyName, 
                                         Model model) {
        // Validate station requirement
        String fromStation = request.getFromStation() == null ? "" : request.getFromStation().trim();
        String toStation = request.getToStation() == null ? "" : request.getToStation().trim();
        
        if (fromStation.isEmpty() || toStation.isEmpty()) {
            model.addAttribute("error", "Both 'From' and 'To' stations are required for train search.");
            model.addAttribute("searchRequest", request);
            
            // Add authentication information
            try {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof String && !"anonymousUser".equals(principal)) {
                    String username = (String) principal;
                    Passenger passenger = passengerService.findByUsername(username).orElse(null);
                    if (passenger != null) {
                        model.addAttribute("currentUser", passenger);
                        model.addAttribute("userRole", passenger.getRole());
                        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                        model.addAttribute("isAuthenticated", true);
                        model.addAttribute("username", passenger.getUsername());
                    } else {
                        model.addAttribute("isAuthenticated", false);
                    }
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } catch (Exception e) {
                model.addAttribute("isAuthenticated", false);
            }
            
            return "search";
        }
        
        try {
            // Use specific strategy
            List<TrainSchedule> results = trainService.searchTrainsWithStrategy(request, strategyName);
            model.addAttribute("results", results);
            model.addAttribute("searchRequest", request);
        } catch (Exception e) {
            model.addAttribute("error", "Error using strategy " + strategyName + ": " + e.getMessage());
            model.addAttribute("searchRequest", request);
        }
        
        // Add authentication information
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "search";
    }
    
    /**
     * Compare legacy vs Strategy pattern approach
     */
    @PostMapping("/search/compare")
    public String compareSearchApproaches(@ModelAttribute SearchRequest request, Model model) {
        // Legacy approach
        List<TrainSchedule> legacyResults = trainService.searchTrainsLegacy(request);
        
        // Strategy pattern approach
        List<TrainSchedule> strategyResults = trainService.searchTrains(request);
        
        model.addAttribute("searchRequest", request);
        model.addAttribute("legacyResults", legacyResults);
        model.addAttribute("strategyResults", strategyResults);
        
        // Add authentication information
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "search-comparison";
    }

    @GetMapping("/seats/{scheduleId}")
    public String showAvailableSeats(@PathVariable Long scheduleId, Model model) {
        // Get schedule information to pass route details
        TrainSchedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            model.addAttribute("error", "Schedule not found");
            return "error";
        }
        
        List<com.trainmanagement.trainmanagementsystem.entity.Seat> seats = trainService.getSeatsForSchedule(scheduleId);
        System.out.println("Loading seats for schedule " + scheduleId + ": " + seats.size() + " seats found");
        for (com.trainmanagement.trainmanagementsystem.entity.Seat seat : seats) {
            System.out.println("Seat: " + seat.getSeatNumber() + " (Coach: " + seat.getCoachNum() + ", Available: " + seat.isAvailable() + ")");
        }
        
        // Add route information for pricing calculation
        String fromStation = schedule.getFromStation();
        String toStation = schedule.getToStation();
        String route = fromStation + " to " + toStation;
        
        // Debug: Log route information
        System.out.println("TrainController - Schedule ID: " + scheduleId);
        System.out.println("TrainController - From Station: " + fromStation);
        System.out.println("TrainController - To Station: " + toStation);
        System.out.println("TrainController - Route: " + route);
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("seats", seats);
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("fromStation", fromStation);
        model.addAttribute("toStation", toStation);
        model.addAttribute("route", route);
        model.addAttribute("bookingRequest", new com.trainmanagement.trainmanagementsystem.dto.BookingRequest());
        
        // Add seatIdToNumberMap for JS mapping
        java.util.Map<Long, String> seatIdToNumberMap = seats.stream()
                .collect(java.util.stream.Collectors.toMap(
                    com.trainmanagement.trainmanagementsystem.entity.Seat::getId, 
                    com.trainmanagement.trainmanagementsystem.entity.Seat::getSeatNumber));
        model.addAttribute("seatIdToNumberMap", seatIdToNumberMap);
        
        // Add authentication information for navigation bar
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "booking";  // Renders booking.html
    }
}