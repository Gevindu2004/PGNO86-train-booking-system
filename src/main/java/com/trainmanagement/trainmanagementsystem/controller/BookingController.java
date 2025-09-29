package com.trainmanagement.trainmanagementsystem.controller;


import com.trainmanagement.trainmanagementsystem.dto.BookingRequest;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public String book(@ModelAttribute BookingRequest request, Model model) {
        bookingService.bookTickets(request); // Assuming bookTickets returns void or Booking
        model.addAttribute("booking", "booking"); // Add booking confirmation message or object
        return "booking"; // Show confirmation
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled");
    }
}