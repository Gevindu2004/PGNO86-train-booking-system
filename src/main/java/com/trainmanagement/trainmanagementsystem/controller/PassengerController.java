package com.trainmanagement.trainmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;

import java.util.List;

@Controller
@RequestMapping("/passenger")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private com.trainmanagement.trainmanagementsystem.service.BookingService bookingService;

    @GetMapping({"", "/", "/dashboard"})
    public String passengerDashboard(Model model) {
        try {
            // Get current user authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            System.out.println("[DEBUG] Dashboard - Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] Dashboard - Principal value: " + principal);
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                System.out.println("[DEBUG] Dashboard - Username: " + username);
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    System.out.println("[DEBUG] Dashboard - Found passenger: " + passenger.getFullName());
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("isAuthenticated", true);
                } else {
                    System.out.println("[DEBUG] Dashboard - Passenger not found for username: " + username);
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                System.out.println("[DEBUG] Dashboard - User not authenticated or anonymous");
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            System.out.println("Error in passengerDashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("isAuthenticated", false);
        }
        
        return "passenger-dashboard";
    }

    @GetMapping("/bookings")
    public String showMyBookings(Model model) {
        try {
            // Get current user
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            System.out.println("[DEBUG] Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] Principal value: " + principal);
            
            // Check if user is not authenticated (anonymous user)
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                System.out.println("[DEBUG] User not authenticated, redirecting to login");
                return "redirect:/login?redirect=/passenger/bookings";
            }
            
            String passengerName = null;
            String username = null;
            
            if (principal instanceof com.trainmanagement.trainmanagementsystem.entity.Passenger) {
                passengerName = ((com.trainmanagement.trainmanagementsystem.entity.Passenger) principal).getFullName();
                username = ((com.trainmanagement.trainmanagementsystem.entity.Passenger) principal).getUsername();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("[DEBUG] Username from UserDetails: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = bookingService.findPassengerByUsername(username);
                if (passenger != null) {
                    passengerName = passenger.getFullName();
                    System.out.println("[DEBUG] Found passenger: " + passengerName);
                } else {
                    System.out.println("[DEBUG] No passenger found for username: " + username);
                }
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                username = (String) principal;
                System.out.println("[DEBUG] Username from String: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = bookingService.findPassengerByUsername(username);
                if (passenger != null) {
                    passengerName = passenger.getFullName();
                    System.out.println("[DEBUG] Found passenger: " + passengerName);
                }
            }
            
            System.out.println("[DEBUG] Final passengerName: " + passengerName);
            
            if (passengerName != null && !passengerName.trim().isEmpty()) {
                List<com.trainmanagement.trainmanagementsystem.entity.Booking> bookings = bookingService.getBookingsByPassengerName(passengerName);
                model.addAttribute("bookings", bookings);
                model.addAttribute("passengerName", passengerName);
                System.out.println("[DEBUG] Found " + bookings.size() + " bookings for " + passengerName);
            } else {
                model.addAttribute("bookings", new java.util.ArrayList<>());
                model.addAttribute("passengerName", username != null ? username : "Guest");
                model.addAttribute("error", "Unable to identify passenger. Please make sure you are logged in.");
                System.out.println("[DEBUG] Unable to identify passenger");
            }
            
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception in showMyBookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("bookings", new java.util.ArrayList<>());
            model.addAttribute("passengerName", "Guest");
            model.addAttribute("error", "Error loading bookings: " + e.getMessage());
        }
        
        return "my-bookings";
    }

    @GetMapping("/health")
    public String health() {
        return "Passenger Controller is working!";
    }

    @GetMapping("/debug-seats")
    public String debugSeats(Model model) {
        try {
            java.util.Map<Long, String> seatMapping = new java.util.HashMap<>();
            java.util.List<String> debugInfo = new java.util.ArrayList<>();
            
            // Get seats for all schedules
            for (long scheduleId = 1; scheduleId <= 3; scheduleId++) {
                try {
                    java.util.List<Seat> seats = trainService.getSeatsForSchedule(scheduleId);
                    debugInfo.add("Schedule " + scheduleId + ": " + seats.size() + " seats");
                    
                    for (Seat seat : seats) {
                        seatMapping.put(seat.getId(), seat.getSeatNumber());
                        debugInfo.add("  Seat ID: " + seat.getId() + " -> Seat Number: " + seat.getSeatNumber());
                    }
                } catch (Exception e) {
                    debugInfo.add("Error loading schedule " + scheduleId + ": " + e.getMessage());
                }
            }
            
            model.addAttribute("seatMapping", seatMapping);
            model.addAttribute("debugInfo", debugInfo);
            model.addAttribute("totalSeats", seatMapping.size());
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "debug-seats";
    }

    @GetMapping("/test-auth")
    public String testAuth(Model model, jakarta.servlet.http.HttpServletRequest request) {
        try {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            model.addAttribute("sessionExists", session != null);
            model.addAttribute("sessionId", session != null ? session.getId() : "No session");
            model.addAttribute("principalType", principal != null ? principal.getClass().getSimpleName() : "null");
            model.addAttribute("principalValue", principal != null ? principal.toString() : "null");
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = passengerService
                        .findByUsername(username).orElse(null);
                if (passenger != null) {
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "test-auth";
    }

    @GetMapping("/auth-status")
    public String authStatus(Model model) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            System.out.println("[DEBUG] Auth Status - Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] Auth Status - Principal value: " + principal);
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username).orElse(null);
                if (passenger != null) {
                    return "Authenticated as: " + passenger.getFullName() + " (" + username + ")";
                }
            }
            
            return "Not authenticated";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/booking")
    public String showPassengerBooking(Model model, jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Check if user is authenticated by looking at session
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            System.out.println("[DEBUG] Session exists: " + (session != null));
            if (session != null) {
                System.out.println("[DEBUG] Session ID: " + session.getId());
                System.out.println("[DEBUG] Session attributes: " + java.util.Collections.list(session.getAttributeNames()));
            }
            
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            System.out.println("[DEBUG] Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] Principal value: " + principal);
            
            String username = null;
            if (principal instanceof com.trainmanagement.trainmanagementsystem.entity.Passenger) {
                username = ((com.trainmanagement.trainmanagementsystem.entity.Passenger) principal).getUsername();
                System.out.println("[DEBUG] Principal is Passenger object, username: " + username);
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("[DEBUG] Principal is UserDetails, username: " + username);
            } else if (principal instanceof String) {
                username = (String) principal;
                System.out.println("[DEBUG] Principal is String, username: " + username);
            } else {
                System.out.println("[DEBUG] Principal is unknown type: " + (principal != null ? principal.getClass() : "null"));
            }
            
            if (username != null && !username.equals("anonymousUser")) {
                System.out.println("[DEBUG] Looking up passenger for username: " + username);
                com.trainmanagement.trainmanagementsystem.entity.Passenger passenger = passengerService
                        .findByUsername(username).orElse(null);
                if (passenger != null) {
                    System.out.println("[DEBUG] Passenger found: " + passenger.getFullName() + ", " + passenger.getEmail()
                            + ", " + passenger.getPhone());
                    model.addAttribute("passengerName", passenger.getFullName());
                    model.addAttribute("passengerEmail", passenger.getEmail());
                    model.addAttribute("passengerPhone", passenger.getPhone());
                } else {
                    System.out.println("[DEBUG] No passenger found for username: " + username);
                    model.addAttribute("passengerName", "Guest User");
                    model.addAttribute("passengerEmail", "Not logged in");
                    model.addAttribute("passengerPhone", "Not logged in");
                }
            } else {
                System.out.println("[DEBUG] Username is null or anonymous in security context");
                model.addAttribute("passengerName", "Guest User");
                model.addAttribute("passengerEmail", "Not logged in");
                model.addAttribute("passengerPhone", "Not logged in");
            }

            // Add seat mapping data for JavaScript
            // We'll create a comprehensive seat mapping for all schedules
            // This will help the JavaScript properly map seat IDs to seat numbers
            try {
                java.util.Map<String, String> globalSeatIdToNumberMap = new java.util.HashMap<>();
                
                // Get seats for all schedules (1, 2, 3 based on your data.sql)
                for (long scheduleId = 1; scheduleId <= 3; scheduleId++) {
                    try {
                        java.util.List<Seat> seats = trainService.getSeatsForSchedule(scheduleId);
                        System.out.println("[DEBUG] Loading " + seats.size() + " seats for schedule " + scheduleId);
                        for (Seat seat : seats) {
                            // Convert Long to String for JavaScript compatibility
                            String seatIdStr = String.valueOf(seat.getId());
                            globalSeatIdToNumberMap.put(seatIdStr, seat.getSeatNumber());
                            System.out.println("[DEBUG] Mapped seat ID " + seatIdStr + " to seat number " + seat.getSeatNumber());
                        }
                    } catch (Exception e) {
                        System.out.println("[DEBUG] Could not load seats for schedule " + scheduleId + ": " + e.getMessage());
                    }
                }
                
                model.addAttribute("seatIdToNumberMap", globalSeatIdToNumberMap);
                System.out.println("[DEBUG] Created seat mapping with " + globalSeatIdToNumberMap.size() + " entries");
                
            } catch (Exception e) {
                System.out.println("[DEBUG] Error creating seat mapping: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("seatIdToNumberMap", new java.util.HashMap<>());
            }
            
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception in showPassengerBooking: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("passengerName", "Error loading user");
            model.addAttribute("passengerEmail", "Error loading user");
            model.addAttribute("passengerPhone", "Error loading user");
            model.addAttribute("seatIdToNumberMap", new java.util.HashMap<>());
        }
        return "passenger-booking";
    }

    @PostMapping("/register/passenger")
    public String registerPassenger(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        // Basic validation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        try {
            passengerService.registerPassenger(fullName, email, phone, username, password);
            redirectAttributes.addAttribute("success", "Registration successful! You can now log in.");
            return "redirect:/register";
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        try {
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                model.addAttribute("passenger", passenger);
                return "profile";
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error loading profile: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                // Update passenger information
                passenger.setFullName(fullName);
                passenger.setEmail(email);
                passenger.setPhone(phone);
                
                passengerService.updatePassenger(passenger);
                
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
                return "redirect:/passenger/profile";
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/passenger/profile";
        }
    }
}
