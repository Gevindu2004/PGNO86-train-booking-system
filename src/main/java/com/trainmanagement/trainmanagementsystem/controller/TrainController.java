package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.dto.SearchRequest;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import com.trainmanagement.trainmanagementsystem.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/trains")
public class TrainController {
    @Autowired
    private TrainService trainService;

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        // Ensure form-backing bean exists
        model.addAttribute("searchRequest", new SearchRequest());
        return "search";  // Renders search.html
    }

    // Optional: root redirect to search page
    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/trains/search";
    }

    @PostMapping("/search")
    public String searchTrains(@ModelAttribute SearchRequest request, Model model) {
        List<TrainSchedule> results = trainService.searchTrains(request);
        model.addAttribute("results", results);
        return "search";  // Show results in same page
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
        
        return "booking";  // Renders booking.html
    }
}