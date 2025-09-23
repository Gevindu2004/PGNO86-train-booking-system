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
    public String showBookingPage(@PathVariable Long scheduleId, @RequestParam(required = false) String coachNum,
            Model model) {
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            model.addAttribute("error", "Schedule not found");
            return "error"; // Create an error page if needed
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleId", scheduleId); // <-- Ensure scheduleId is added to the model
        List<Seat> seats = trainService.getSeatsForSchedule(scheduleId); // Fetch fresh data
        model.addAttribute("seats", seats);
        // Add seatIdToNumberMap for JS mapping
        java.util.Map<Long, String> seatIdToNumberMap = seats.stream()
                .collect(java.util.stream.Collectors.toMap(Seat::getId, Seat::getSeatNumber));
        model.addAttribute("seatIdToNumberMap", seatIdToNumberMap);
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

    @PostMapping("/complete")
    public String completeBooking(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) String seatIds,
            Model model) {

        // Validate input parameters
        if (scheduleId == null) {
            model.addAttribute("error", "Schedule ID is missing");
            return "passenger-booking";
        }

        if (seatIds == null || seatIds.trim().isEmpty()) {
            model.addAttribute("error", "No seats selected");
            return "passenger-booking";
        }

        try {
            // Parse seat IDs
            String[] seatIdArray = seatIds.split(",");
            Long[] seatIdLongs = new Long[seatIdArray.length];
            for (int i = 0; i < seatIdArray.length; i++) {
                seatIdLongs[i] = Long.parseLong(seatIdArray[i].trim());
            }

            // Fetch seat entities for mapping
            List<Seat> selectedSeats = trainService.getSeatsForSchedule(scheduleId).stream()
                    .filter(seat -> java.util.Arrays.asList(seatIdLongs).contains(seat.getId()))
                    .toList();
            java.util.Map<Long, String> seatIdToNumberMap = selectedSeats.stream()
                    .collect(java.util.stream.Collectors.toMap(Seat::getId, Seat::getSeatNumber));
            model.addAttribute("seatIdToNumberMap", seatIdToNumberMap);

            // Create booking request
            BookingRequest request = new BookingRequest();
            request.setScheduleId(scheduleId);
            request.setSeatIds(java.util.Arrays.asList(seatIdLongs));
            // Get logged-in passenger from security context
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            System.out.println("[DEBUG] BookingController - Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] BookingController - Principal value: " + principal);
            
            if (principal instanceof com.trainmanagement.trainmanagementsystem.entity.Passenger) {
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = (com.trainmanagement.trainmanagementsystem.entity.Passenger) principal;
                request.setPassengerName(passenger.getFullName());
                model.addAttribute("passengerName", passenger.getFullName());
                model.addAttribute("passengerEmail", passenger.getEmail());
                model.addAttribute("passengerPhone", passenger.getPhone());
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("[DEBUG] Looking up passenger for username: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = bookingService
                        .findPassengerByUsername(username);
                if (passenger != null) {
                    request.setPassengerName(passenger.getFullName());
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                    System.out.println("[DEBUG] BookingController - Set passenger name: " + passenger.getFullName());
                } else {
                    model.addAttribute("passengerName", username);
                    model.addAttribute("passengerEmail", "-");
                    model.addAttribute("passengerPhone", "-");
                    request.setPassengerName(username);
                    System.out.println("[DEBUG] BookingController - Set username as passenger name: " + username);
                }
            } else if (principal instanceof String && "anonymousUser".equals(principal)) {
                // Not logged in, redirect to login page
                return "redirect:/login";
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                // Principal is a username string
                String username = (String) principal;
                System.out.println("[DEBUG] BookingController - Username from String: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = bookingService
                        .findPassengerByUsername(username);
                if (passenger != null) {
                    request.setPassengerName(passenger.getFullName());
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                    System.out.println("[DEBUG] BookingController - Set passenger name from String: " + passenger.getFullName());
                } else {
                    model.addAttribute("passengerName", username);
                    model.addAttribute("passengerEmail", "-");
                    model.addAttribute("passengerPhone", "-");
                    request.setPassengerName(username);
                    System.out.println("[DEBUG] BookingController - Set username as passenger name from String: " + username);
                }
            } else {
                model.addAttribute("passengerName", "-");
                model.addAttribute("passengerEmail", "-");
                model.addAttribute("passengerPhone", "-");
                request.setPassengerName("-");
                System.out.println("[DEBUG] BookingController - Unknown principal type, setting passenger name to '-'");
            }

            // Process booking
            Booking booking = bookingService.bookTickets(request);
            
            // Add booking details to model for success page
            model.addAttribute("booking", booking);

            // Add seat numbers for display
            List<String> seatNumbers = booking.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("seatNumbers", seatNumbers);

            // Add seat numbers for display (comma-separated string for easy display)
            String seatNumbersString = booking.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.joining(", "));
            model.addAttribute("seatNumbersString", seatNumbersString);

            // Redirect to success page
            return "booking-success";

        } catch (Exception e) {
            model.addAttribute("error", "Booking failed: " + e.getMessage());
            return "passenger-booking";
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled");
    }
}