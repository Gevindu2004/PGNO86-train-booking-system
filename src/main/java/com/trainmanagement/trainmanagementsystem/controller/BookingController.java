package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.dto.BookingRequest;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.entity.Schedule;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.service.BookingService;
import com.trainmanagement.trainmanagementsystem.service.ScheduleService;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TrainService trainService;

    @GetMapping("/{scheduleId}")
    public String showBookingPage(@PathVariable Long scheduleId, @RequestParam(required = false) String coachNum, Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            model.addAttribute("error", "Schedule not found");
            return "error"; // Create an error page if needed
        }
        model.addAttribute("schedule", schedule);
        List<Seat> seats = trainService.getSeatsForSchedule(scheduleId); // Fetch fresh data
        model.addAttribute("seats", seats);
        // If coachNum is provided, filter seats by coach (optional enhancement)
        if (coachNum != null && !coachNum.isEmpty()) {
            seats = seats.stream().filter(seat -> seat.getCoachNum().equals(coachNum)).toList();
            model.addAttribute("seats", seats);
        }
        return "booking"; // Renders booking.html
    }

    @PostMapping
    public String book(@ModelAttribute BookingRequest request, Model model) {
        Booking booking = bookingService.bookTickets(request);
        model.addAttribute("booking", booking); // Update with actual booking object if needed
        return "booking"; // Or redirect to confirmation page
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled");
    }
}