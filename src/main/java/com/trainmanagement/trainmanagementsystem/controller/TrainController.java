package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.dto.SearchRequest;
import com.trainmanagement.trainmanagementsystem.entity.Schedule;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
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
        List<Schedule> results = trainService.searchTrains(request);
        model.addAttribute("results", results);
        return "search";  // Show results in same page
    }

    @GetMapping("/seats/{scheduleId}")
    public String showAvailableSeats(@PathVariable Long scheduleId, Model model) {
        model.addAttribute("seats", trainService.getAvailableSeats(scheduleId));
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("bookingRequest", new com.trainmanagement.trainmanagementsystem.dto.BookingRequest());
        return "booking";  // Renders booking.html
    }
}