package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.dto.BookingRequest;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.service.BookingService;
import com.trainmanagement.trainmanagementsystem.service.ScheduleService;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import com.trainmanagement.trainmanagementsystem.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private PricingService pricingService;

    @GetMapping("/{scheduleId}")
    public String showBookingPage(@PathVariable Long scheduleId, @RequestParam(required = false) String coachNum,
            Model model) {
        TrainSchedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            model.addAttribute("error", "Schedule not found");
            return "error"; // Create an error page if needed
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleId", scheduleId); // <-- Ensure scheduleId is added to the model
        List<Seat> seats = trainService.getSeatsForSchedule(scheduleId); // Fetch fresh data
        model.addAttribute("seats", seats);

        java.util.Map<Long, String> seatIdToNumberMap = seats.stream()
                .collect(java.util.stream.Collectors.toMap(Seat::getId, Seat::getSeatNumber));
        model.addAttribute("seatIdToNumberMap", seatIdToNumberMap);
        
        // Add route information for pricing calculation
        String fromStation = schedule.getFromStation();
        String toStation = schedule.getToStation();
        String route = fromStation + " to " + toStation;
        model.addAttribute("fromStation", fromStation);
        model.addAttribute("toStation", toStation);
        model.addAttribute("route", route);
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
            Model model,
            HttpServletRequest request) {

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
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setScheduleId(scheduleId);
            bookingRequest.setSeatIds(java.util.Arrays.asList(seatIdLongs));
            // Get logged-in passenger from security context
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            System.out.println("[DEBUG] BookingController - Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] BookingController - Principal value: " + principal);
            
            if (principal instanceof com.trainmanagement.trainmanagementsystem.entity.Passenger) {
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = (com.trainmanagement.trainmanagementsystem.entity.Passenger) principal;
                bookingRequest.setPassengerName(passenger.getFullName());
                model.addAttribute("passengerName", passenger.getFullName());
                model.addAttribute("passengerEmail", passenger.getEmail());
                model.addAttribute("passengerPhone", passenger.getPhone());
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("[DEBUG] Looking up passenger for username: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = bookingService
                        .findPassengerByUsername(username);
                if (passenger != null) {
                    bookingRequest.setPassengerName(passenger.getFullName());
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                    System.out.println("[DEBUG] BookingController - Set passenger name: " + passenger.getFullName());
                } else {
                    model.addAttribute("passengerName", username);
                    model.addAttribute("passengerEmail", "-");
                    model.addAttribute("passengerPhone", "-");
                    bookingRequest.setPassengerName(username);
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
                    bookingRequest.setPassengerName(passenger.getFullName());
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                    System.out.println("[DEBUG] BookingController - Set passenger name from String: " + passenger.getFullName());
                } else {
                    model.addAttribute("passengerName", username);
                    model.addAttribute("passengerEmail", "-");
                    model.addAttribute("passengerPhone", "-");
                    bookingRequest.setPassengerName(username);
                    System.out.println("[DEBUG] BookingController - Set username as passenger name from String: " + username);
                }
            } else {
                model.addAttribute("passengerName", "-");
                model.addAttribute("passengerEmail", "-");
                model.addAttribute("passengerPhone", "-");
                bookingRequest.setPassengerName("-");
                System.out.println("[DEBUG] BookingController - Unknown principal type, setting passenger name to '-'");
            }

            // Don't process booking yet - just prepare data for display
            // Store booking data in session for later confirmation
            request.getSession().setAttribute("pendingBookingRequest", bookingRequest);
            request.getSession().setAttribute("selectedSeats", selectedSeats);
            
            // Add seat numbers for display
            List<String> seatNumbers = selectedSeats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("seatNumbers", seatNumbers);

            // Add seat numbers for display (comma-separated string for easy display)
            String seatNumbersString = selectedSeats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.joining(", "));
            model.addAttribute("seatNumbersString", seatNumbersString);
            
            // Create a temporary booking object for display purposes only
            Booking tempBooking = new Booking();
            tempBooking.setId(0L); // Temporary ID
            tempBooking.setPassengerName(bookingRequest.getPassengerName());
            tempBooking.setStatus("PENDING");
            tempBooking.setBookingTime(java.time.LocalDateTime.now());
            
            // Get schedule for display
            TrainSchedule schedule = scheduleService.findById(scheduleId);
            tempBooking.setSchedule(schedule);
            tempBooking.setSeats(selectedSeats);
            
            model.addAttribute("booking", tempBooking);

            // Calculate pricing information
            if (!selectedSeats.isEmpty()) {
                // Get the first seat to determine class type
                String firstSeatNumber = selectedSeats.get(0).getSeatNumber();
                String classType = firstSeatNumber.substring(0, 1); // A, B, or C
                
                // Determine class name
                String className;
                switch (classType) {
                    case "A":
                        className = "First Class";
                        break;
                    case "B":
                        className = "Second Class";
                        break;
                    case "C":
                        className = "Third Class";
                        break;
                    default:
                        className = "Class " + classType;
                }
                
                // Get route information
                String fromStation = schedule.getFromStation();
                String toStation = schedule.getToStation();
                
                // Calculate pricing based on route and class using database
                double pricePerSeat = pricingService.getPrice(fromStation, toStation, classType);
                int seatCount = selectedSeats.size();
                double totalAmount = pricePerSeat * seatCount;
                
                // Add pricing information to model
                model.addAttribute("classType", className);
                model.addAttribute("pricePerSeat", String.format("%.2f", pricePerSeat));
                model.addAttribute("seatCount", seatCount);
                model.addAttribute("totalAmount", String.format("%.2f", totalAmount));
            }

            // Redirect to success page
            return "booking-success";

        } catch (Exception e) {
            model.addAttribute("error", "Booking failed: " + e.getMessage());
            return "passenger-booking";
        }
    }

    @PostMapping("/confirm")
    public String confirmBooking(Model model, HttpServletRequest request) {
        try {
            // Get pending booking data from session
            BookingRequest bookingRequest = (BookingRequest) request.getSession().getAttribute("pendingBookingRequest");
            @SuppressWarnings("unchecked")
            List<Seat> selectedSeats = (List<Seat>) request.getSession().getAttribute("selectedSeats");
            
            if (bookingRequest == null || selectedSeats == null) {
                model.addAttribute("error", "No pending booking found. Please start a new booking.");
                return "redirect:/trains/search";
            }
            
            // Now actually create the booking and reserve seats
            Booking booking = bookingService.bookTickets(bookingRequest);
            
            // Clear session data
            request.getSession().removeAttribute("pendingBookingRequest");
            request.getSession().removeAttribute("selectedSeats");
            
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

            // Calculate pricing information
            if (!booking.getSeats().isEmpty()) {
                // Get the first seat to determine class type
                String firstSeatNumber = booking.getSeats().get(0).getSeatNumber();
                String classType = firstSeatNumber.substring(0, 1); // A, B, or C
                
                // Determine class name
                String className;
                switch (classType) {
                    case "A":
                        className = "First Class";
                        break;
                    case "B":
                        className = "Second Class";
                        break;
                    case "C":
                        className = "Third Class";
                        break;
                    default:
                        className = "Class " + classType;
                }
                
                // Get route information
                String fromStation = booking.getSchedule().getFromStation();
                String toStation = booking.getSchedule().getToStation();
                
                // Calculate pricing based on route and class using database
                double pricePerSeat = pricingService.getPrice(fromStation, toStation, classType);
                int seatCount = booking.getSeats().size();
                double totalAmount = pricePerSeat * seatCount;
                
                // Add pricing information to model
                model.addAttribute("classType", className);
                model.addAttribute("pricePerSeat", String.format("%.2f", pricePerSeat));
                model.addAttribute("seatCount", seatCount);
                model.addAttribute("totalAmount", String.format("%.2f", totalAmount));
            }

            // Redirect to success page with confirmed booking
            return "redirect:/bookings/success/" + booking.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Booking confirmation failed: " + e.getMessage());
            return "redirect:/trains/search";
        }
    }

    @GetMapping("/success/{id}")
    public String showBookingSuccess(@PathVariable Long id, Model model) {
        try {
            // Get the confirmed booking
            Booking booking = bookingService.findById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/trains/search";
            }
            
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

            // Calculate pricing information
            if (!booking.getSeats().isEmpty()) {
                // Get the first seat to determine class type
                String firstSeatNumber = booking.getSeats().get(0).getSeatNumber();
                String classType = firstSeatNumber.substring(0, 1); // A, B, or C
                
                // Determine class name
                String className;
                switch (classType) {
                    case "A":
                        className = "First Class";
                        break;
                    case "B":
                        className = "Second Class";
                        break;
                    case "C":
                        className = "Third Class";
                        break;
                    default:
                        className = "Class " + classType;
                }
                
                // Get route information
                String fromStation = booking.getSchedule().getFromStation();
                String toStation = booking.getSchedule().getToStation();
                
                // Calculate pricing based on route and class using database
                double pricePerSeat = pricingService.getPrice(fromStation, toStation, classType);
                int seatCount = booking.getSeats().size();
                double totalAmount = pricePerSeat * seatCount;
                
                // Add pricing information to model
                model.addAttribute("classType", className);
                model.addAttribute("pricePerSeat", String.format("%.2f", pricePerSeat));
                model.addAttribute("seatCount", seatCount);
                model.addAttribute("totalAmount", String.format("%.2f", totalAmount));
            }
            
            // Add flag to show popup
            model.addAttribute("showPopup", true);
            
            return "booking-success";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking details: " + e.getMessage());
            return "redirect:/trains/search";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, Model model) {
        try {
            bookingService.cancelBooking(id);
            return "redirect:/bookings/cancel-success/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel booking: " + e.getMessage());
            return "redirect:/passenger/bookings";
        }
    }
    
    @GetMapping("/cancel-success/{id}")
    public String showCancelSuccess(@PathVariable Long id, Model model) {
        try {
            // Get the cancelled booking details for display
            Booking booking = bookingService.findById(id);
            if (booking == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/passenger/bookings";
            }
            
            model.addAttribute("booking", booking);
            model.addAttribute("passengerName", booking.getPassengerName());
            
            // Add seat numbers for display
            String seatNumbersString = booking.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.joining(", "));
            model.addAttribute("seatNumbersString", seatNumbersString);
            
            return "booking-cancel-success";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading booking details: " + e.getMessage());
            return "redirect:/passenger/bookings";
        }
    }
    
    
//pricing validations
    @GetMapping("/pricing/{fromStation}/{toStation}/{classType}")
    public ResponseEntity<Double> getPricing(@PathVariable String fromStation, 
                                           @PathVariable String toStation, 
                                           @PathVariable String classType) {
        try {
            System.out.println("Pricing API called - From: " + fromStation + ", To: " + toStation + ", Class: " + classType);
            double price = pricingService.getPrice(fromStation, toStation, classType);
            System.out.println("Pricing API result: " + price);
            return ResponseEntity.ok(price);
        } catch (Exception e) {
            System.err.println("Pricing API error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(0.0);
        }
    }
}