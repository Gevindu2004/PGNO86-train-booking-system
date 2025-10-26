package com.trainmanagement.trainmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.service.AlertService;
import com.trainmanagement.trainmanagementsystem.service.TrainService;
import com.trainmanagement.trainmanagementsystem.service.PricingService;
import com.trainmanagement.trainmanagementsystem.service.TrainScheduleService;
import com.trainmanagement.trainmanagementsystem.service.BookingService;
import com.trainmanagement.trainmanagementsystem.service.TicketPdfService;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import com.trainmanagement.trainmanagementsystem.repository.PassengerRepository;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;

import java.util.List;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/passenger")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private com.trainmanagement.trainmanagementsystem.service.BookingService bookingService;

    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private TrainScheduleService trainScheduleService;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private RoleBasedAccessControl rbac;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private TicketPdfService ticketPdfService;

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
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
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
        
        // Get recent alerts for sidebar - system-critical alerts for all users, personal alerts for logged-in users
        List<Alert> recentAlerts;
        try {
            if (model.containsAttribute("currentUser") && model.getAttribute("currentUser") != null) {
                // User is logged in - show system-critical alerts + their personal alerts
                Passenger currentUser = (Passenger) model.getAttribute("currentUser");
                if (currentUser != null) {
                    try {
                        // Get alerts for logged-in user (system-critical + personal)
                        recentAlerts = alertService.findAlertsForUser(currentUser).stream()
                                .limit(5) // Show only 5 most recent alerts
                                .collect(java.util.stream.Collectors.toList());
                        System.out.println("[DEBUG] Dashboard - Loaded " + recentAlerts.size() + " alerts for logged-in user: " + currentUser.getUsername());
                    } catch (Exception e) {
                        System.err.println("[ERROR] Dashboard - Error loading user alerts: " + e.getMessage());
                        e.printStackTrace();
                        // Fallback to system-critical alerts only
                        recentAlerts = alertService.findSystemCriticalAlertsSorted().stream()
                                .limit(5)
                                .collect(java.util.stream.Collectors.toList());
                        System.out.println("[DEBUG] Dashboard - Using system-critical alerts only due to error");
                    }
                } else {
                    // Fallback to system-critical alerts only
                    recentAlerts = alertService.findSystemCriticalAlertsSorted().stream()
                            .limit(5)
                            .collect(java.util.stream.Collectors.toList());
                    System.out.println("[DEBUG] Dashboard - Using system-critical alerts only (currentUser was null)");
                }
            } else {
                // Guest user - show system-critical alerts (delays, maintenance, cancellations)
                recentAlerts = alertService.findAlertsForGuest().stream()
                        .limit(5) // Show only 5 most recent alerts
                        .collect(java.util.stream.Collectors.toList());
                System.out.println("[DEBUG] Dashboard - Loaded " + recentAlerts.size() + " system-critical alerts for guest user");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Dashboard - Error loading alerts: " + e.getMessage());
            e.printStackTrace();
            // Fallback to empty list if all else fails
            recentAlerts = new java.util.ArrayList<>();
            System.out.println("[DEBUG] Dashboard - Using empty alerts list due to error");
        }
        model.addAttribute("recentAlerts", recentAlerts);
        
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
                
                // Create a map to store pricing information for each booking
                java.util.Map<Long, java.util.Map<String, Object>> bookingPricing = new java.util.HashMap<>();
                
                // Create a map to store modification status for each booking
                java.util.Map<Long, String> bookingModificationStatus = new java.util.HashMap<>();
                
                // Calculate pricing information and modification status for each booking
                for (com.trainmanagement.trainmanagementsystem.entity.Booking booking : bookings) {
                    // Get modification status for this booking
                    String modificationStatus = bookingService.getBookingModificationStatus(booking.getId());
                    bookingModificationStatus.put(booking.getId(), modificationStatus);
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
                        
                        // Store pricing information in the map
                        java.util.Map<String, Object> pricingInfo = new java.util.HashMap<>();
                        pricingInfo.put("classType", className);
                        pricingInfo.put("pricePerSeat", String.format("%.2f", pricePerSeat));
                        pricingInfo.put("seatCount", seatCount);
                        pricingInfo.put("totalAmount", String.format("%.2f", totalAmount));
                        
                        bookingPricing.put(booking.getId(), pricingInfo);
                    }
                }
                
                model.addAttribute("bookings", bookings);
                model.addAttribute("bookingPricing", bookingPricing);
                model.addAttribute("bookingModificationStatus", bookingModificationStatus);
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
                    
                    // Add authentication status and user role information
                    model.addAttribute("isAuthenticated", true);
                    model.addAttribute("currentUser", passenger);
                    model.addAttribute("userRole", passenger.getRole());
                    model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(passenger.getRole()));
                    model.addAttribute("username", passenger.getUsername());
                } else {
                    System.out.println("[DEBUG] No passenger found for username: " + username);
                    model.addAttribute("passengerName", "Guest User");
                    model.addAttribute("passengerEmail", "Not logged in");
                    model.addAttribute("passengerPhone", "Not logged in");
                    model.addAttribute("isAuthenticated", false);
                }
            } else {
                System.out.println("[DEBUG] Username is null or anonymous in security context");
                model.addAttribute("passengerName", "Guest User");
                model.addAttribute("passengerEmail", "Not logged in");
                model.addAttribute("passengerPhone", "Not logged in");
                model.addAttribute("isAuthenticated", false);
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
        
        // Phone number validation
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Phone number is required.");
            return "redirect:/register";
        }
        
        // Remove any non-digit characters and check length
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() != 10) {
            redirectAttributes.addAttribute("error", "Phone number must be exactly 10 digits.");
            return "redirect:/register";
        }
        
        // Additional validations
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Full name is required.");
            return "redirect:/register";
        }
        
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Email is required.");
            return "redirect:/register";
        }
        
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Username is required.");
            return "redirect:/register";
        }
        
        try {
            // Use cleaned phone number for registration
            passengerService.registerPassenger(fullName.trim(), email.trim(), cleanPhone, username.trim(), password);
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
            System.out.println("=== DEBUG: Profile page accessed ===");
            
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("Principal: " + principal);
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                System.out.println("Username: " + username);
                
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                System.out.println("Passenger found: " + passenger.getFullName());
                model.addAttribute("passenger", passenger);
                model.addAttribute("debugMessage", "Profile page loaded successfully for: " + passenger.getFullName());
                return "profile";
            } else {
                System.out.println("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error loading profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading profile: " + e.getMessage());
            return "profile"; // Return profile template even with error
        }
    }

    @GetMapping("/profile-test")
    public String showProfileTest(Model model) {
        try {
            System.out.println("=== DEBUG: Profile test page accessed ===");
            
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("Principal: " + principal);
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                System.out.println("Username: " + username);
                
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                System.out.println("Passenger found: " + passenger.getFullName());
                model.addAttribute("passenger", passenger);
                model.addAttribute("debugMessage", "Profile test page loaded successfully for: " + passenger.getFullName());
                return "profile-test";
            } else {
                System.out.println("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error loading profile test: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading profile test: " + e.getMessage());
            return "profile-test";
        }
    }

    @GetMapping("/profile-simple")
    public String showProfileSimple(Model model) {
        try {
            System.out.println("=== DEBUG: Profile simple page accessed ===");
            
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("Principal: " + principal);
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                System.out.println("Username: " + username);
                
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                System.out.println("Passenger found: " + passenger.getFullName());
                model.addAttribute("passenger", passenger);
                model.addAttribute("debugMessage", "Profile simple page loaded successfully for: " + passenger.getFullName());
                return "profile-simple";
            } else {
                System.out.println("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error loading profile simple: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading profile simple: " + e.getMessage());
            return "profile-simple";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam(required = false) String currentPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                // Validate input
                if (fullName == null || fullName.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Full name is required");
                    return "redirect:/passenger/profile";
                }
                
                if (email == null || email.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Email is required");
                    return "redirect:/passenger/profile";
                }
                
                if (phone == null || phone.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Phone number is required");
                    return "redirect:/passenger/profile";
                }
                
                // Check if email is already taken by another user
                if (!passenger.getEmail().equals(email) && passengerService.existsByEmail(email)) {
                    redirectAttributes.addFlashAttribute("error", "Email is already taken by another user");
                    return "redirect:/passenger/profile";
                }
                
                // Verify current password if provided
                if (currentPassword != null && !currentPassword.trim().isEmpty()) {
                    if (!passengerService.authenticate(username, currentPassword)) {
                        redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                        return "redirect:/passenger/profile";
                    }
                }
                
                // Update passenger information
                passenger.setFullName(fullName.trim());
                passenger.setEmail(email.trim());
                passenger.setPhone(phone.trim());
                
                passengerService.updatePassenger(passenger);
                
                System.out.println("Profile updated successfully for user: " + username);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
                return "redirect:/passenger/profile";
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/passenger/profile";
        }
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                String username = (String) principal;
                Passenger passenger = passengerService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));
                
                // Validate input
                if (oldPassword == null || oldPassword.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Current password is required");
                    return "redirect:/passenger/profile";
                }
                
                if (newPassword == null || newPassword.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "New password is required");
                    return "redirect:/passenger/profile";
                }
                
                if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Password confirmation is required");
                    return "redirect:/passenger/profile";
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                    return "redirect:/passenger/profile";
                }
                
                if (newPassword.length() < 8) {
                    redirectAttributes.addFlashAttribute("error", "New password must be at least 8 characters long");
                    return "redirect:/passenger/profile";
                }
                
                // Verify current password
                if (!passengerService.authenticate(username, oldPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                    return "redirect:/passenger/profile";
                }
                
                // Update password
                passengerService.updateUserPassword(passenger.getId(), newPassword);
                
                System.out.println("Password changed successfully for user: " + username);
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
                return "redirect:/passenger/profile";
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.out.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
            return "redirect:/passenger/profile";
        }
    }

    @GetMapping("/bookings/{bookingId}/change-seats")
    public String showChangeSeatsForm(@PathVariable Long bookingId, Model model) {
        try {
            System.out.println("DEBUG: Accessing seat change for booking ID: " + bookingId);
            
            // Get current user authentication
            Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            System.out.println("DEBUG: Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                System.out.println("DEBUG: User not authenticated, redirecting to login");
                return "redirect:/login?redirect=/passenger/bookings/" + bookingId + "/change-seats";
            }

            // Get booking details
            System.out.println("DEBUG: Looking up booking with ID: " + bookingId);
            Booking booking = bookingService.findById(bookingId);
            if (booking == null) {
                System.out.println("DEBUG: Booking not found for ID: " + bookingId);
                model.addAttribute("error", "Booking not found");
                return "redirect:/passenger/bookings";
            }

            System.out.println("DEBUG: Found booking: " + booking.getId() + ", Status: " + booking.getStatus());

            // Check booking modification status
            String modificationStatus = bookingService.getBookingModificationStatus(bookingId);
            System.out.println("DEBUG: Booking modification status: " + modificationStatus);
            
            if ("BOOKING_NOT_FOUND".equals(modificationStatus)) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/passenger/bookings";
            } else if ("NOT_CONFIRMED".equals(modificationStatus)) {
                model.addAttribute("error", "This booking is not confirmed and cannot be modified");
                return "redirect:/passenger/bookings";
            } else if ("CAN_ONLY_CANCEL".equals(modificationStatus)) {
                // Calculate time elapsed
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime bookingTime = booking.getBookingTime();
                java.time.Duration duration = java.time.Duration.between(bookingTime, now);
                long minutesElapsed = duration.toMinutes();
                
                model.addAttribute("error", "Cannot change seat after 30 minutes! " +
                    "Your booking was made " + minutesElapsed + " minutes ago. You can only cancel this booking now.");
                return "redirect:/passenger/bookings";
            }

            // Get all seats for the same schedule (both available and occupied)
            System.out.println("DEBUG: Getting all seats for schedule: " + booking.getSchedule().getScheduleId());
            List<Seat> allSeats = seatRepository.findByScheduleIdOrderByCoachAndSeat(booking.getSchedule().getScheduleId());
            List<Seat> availableSeats = allSeats.stream()
                    .filter(Seat::isAvailable)
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("DEBUG: Found " + allSeats.size() + " total seats, " + availableSeats.size() + " available seats");
            
            // Sort all seats by coach (A1, B1, C1) and then by seat number
            allSeats.sort((s1, s2) -> {
                // First sort by coach number (A1, B1, C1)
                int coachComparison = s1.getCoachNum().compareTo(s2.getCoachNum());
                if (coachComparison != 0) {
                    return coachComparison;
                }
                // Then sort by seat number within the same coach (extract numeric part)
                try {
                    // Extract numeric part from seat number (e.g., "A1-W10" -> 10)
                    String seatNum1 = s1.getSeatNumber();
                    String seatNum2 = s2.getSeatNumber();
                    
                    // Find the last dash and extract the number after it
                    int dashIndex1 = seatNum1.lastIndexOf('-');
                    int dashIndex2 = seatNum2.lastIndexOf('-');
                    
                    if (dashIndex1 != -1 && dashIndex2 != -1) {
                        String numPart1 = seatNum1.substring(dashIndex1 + 1);
                        String numPart2 = seatNum2.substring(dashIndex2 + 1);
                        
                        // Remove 'W' prefix if present and convert to integer
                        if (numPart1.startsWith("W")) numPart1 = numPart1.substring(1);
                        if (numPart2.startsWith("W")) numPart2 = numPart2.substring(1);
                        
                        int num1 = Integer.parseInt(numPart1);
                        int num2 = Integer.parseInt(numPart2);
                        
                        return Integer.compare(num1, num2);
                    }
                } catch (Exception e) {
                    // Fallback to string comparison if parsing fails
                    System.out.println("Warning: Could not parse seat numbers for sorting: " + e.getMessage());
                }
                
                // Fallback to string comparison
                return s1.getSeatNumber().compareTo(s2.getSeatNumber());
            });
            
            System.out.println("DEBUG: Sorted all seats: " + 
                allSeats.stream().map(s -> s.getSeatNumber()).collect(java.util.stream.Collectors.joining(", ")));
            
            // Group seats by coach
            List<String> coaches = allSeats.stream()
                    .map(Seat::getCoachNum)
                    .filter(coachNum -> coachNum != null && !coachNum.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());

            System.out.println("DEBUG: Found coaches: " + coaches);

            model.addAttribute("booking", booking);
            model.addAttribute("currentSeats", booking.getSeats());
            model.addAttribute("allSeats", allSeats);
            model.addAttribute("availableSeats", availableSeats);
            model.addAttribute("coaches", coaches);
            model.addAttribute("schedule", booking.getSchedule());
            
            // Get pricing information for the route
            String fromStation = booking.getSchedule().getFromStation();
            String toStation = booking.getSchedule().getToStation();
            
            // Get pricing for different seat classes
            java.util.Map<String, Double> seatPricing = new java.util.HashMap<>();
            
            // Get pricing for each seat class (A, B, C)
            for (String classType : new String[]{"A", "B", "C"}) {
                try {
                    double price = pricingService.getPrice(fromStation, toStation, classType);
                    if (price > 0) {
                        seatPricing.put(classType, price);
                        System.out.println("DEBUG: Pricing for " + fromStation + " to " + toStation + " class " + classType + ": Rs. " + price);
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Could not get pricing for class " + classType + ": " + e.getMessage());
                }
            }
            
            // Calculate pricing for current booking (same logic as booking success page)
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
                
                // Calculate pricing based on route and class using database (same as booking success)
                double pricePerSeat = pricingService.getPrice(fromStation, toStation, classType);
                int seatCount = booking.getSeats().size();
                double totalAmount = pricePerSeat * seatCount;
                
                // Add pricing information to model (same as booking success page)
                model.addAttribute("currentClassType", className);
                model.addAttribute("currentPricePerSeat", String.format("%.2f", pricePerSeat));
                model.addAttribute("currentSeatCount", seatCount);
                model.addAttribute("currentTotalAmount", String.format("%.2f", totalAmount));
                model.addAttribute("currentClassCode", classType);
                
                System.out.println("DEBUG: Current booking pricing - Class: " + className + ", Price per seat: " + pricePerSeat + ", Total: " + totalAmount);
            }
            
            model.addAttribute("seatPricing", seatPricing);
            System.out.println("DEBUG: Added seat pricing to model: " + seatPricing);

            System.out.println("DEBUG: Successfully prepared model, returning change-seats view");
            return "passenger/change-seats";
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in showChangeSeatsForm: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading seat change form: " + e.getMessage());
            return "redirect:/passenger/bookings";
        }
    }

    @PostMapping("/bookings/{bookingId}/change-seats")
    public String processSeatChange(@PathVariable Long bookingId, 
                                  @RequestParam("newSeatIds") List<Long> newSeatIds,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (newSeatIds == null || newSeatIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one seat");
                return "redirect:/passenger/bookings/" + bookingId + "/change-seats";
            }

            // Check if booking can still be modified (time validation)
            String modificationStatus = bookingService.getBookingModificationStatus(bookingId);
            if ("CAN_ONLY_CANCEL".equals(modificationStatus)) {
                Booking booking = bookingService.findById(bookingId);
                if (booking != null) {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime bookingTime = booking.getBookingTime();
                    java.time.Duration duration = java.time.Duration.between(bookingTime, now);
                    long minutesElapsed = duration.toMinutes();
                    
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Cannot change seat after 30 minutes! " +
                        "Your booking was made " + minutesElapsed + " minutes ago. You can only cancel this booking now.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
                }
                return "redirect:/passenger/bookings";
            } else if (!"CAN_MODIFY".equals(modificationStatus)) {
                redirectAttributes.addFlashAttribute("errorMessage", "This booking cannot be modified");
                return "redirect:/passenger/bookings";
            }

            // Update booking seats
            Booking updatedBooking = bookingService.updateBookingSeats(bookingId, newSeatIds);
            
            // Create success message with seat details
            String seatNumbers = updatedBooking.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(java.util.stream.Collectors.joining(", "));
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Seats updated successfully! New seats: " + seatNumbers);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update seats: " + e.getMessage());
            return "redirect:/passenger/bookings/" + bookingId + "/change-seats";
        }

        return "redirect:/passenger/bookings";
    }

    // Debug endpoint to check database data
    @GetMapping("/debug/database")
    @ResponseBody
    public String debugDatabase() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DATABASE DEBUG INFO ===\n\n");
            
            // Check passengers
            result.append("=== PASSENGERS ===\n");
            try {
                List<com.trainmanagement.trainmanagementsystem.entity.Passenger> passengers = passengerRepository.findAll();
                result.append("Total passengers: ").append(passengers.size()).append("\n");
                for (com.trainmanagement.trainmanagementsystem.entity.Passenger passenger : passengers) {
                    result.append("ID: ").append(passenger.getId())
                          .append(", Username: ").append(passenger.getUsername())
                          .append(", Name: ").append(passenger.getFullName())
                          .append(", Email: ").append(passenger.getEmail())
                          .append("\n");
                }
            } catch (Exception e) {
                result.append("Error getting passengers: ").append(e.getMessage()).append("\n");
            }
            
            result.append("\n=== BOOKINGS ===\n");
            try {
                // Try to get bookings for different passengers
                String[] testPassengers = {"admin", "john", "sarah", "test"};
                for (String passengerName : testPassengers) {
                    List<Booking> bookings = bookingService.getBookingsByPassengerName(passengerName);
                    result.append("Bookings for '").append(passengerName).append("': ").append(bookings.size()).append("\n");
                    for (Booking booking : bookings) {
                        result.append("  - Booking ID: ").append(booking.getId())
                              .append(", Status: ").append(booking.getStatus())
                              .append(", Schedule ID: ").append(booking.getSchedule() != null ? booking.getSchedule().getScheduleId() : "null")
                              .append(", Seats: ").append(booking.getSeats() != null ? booking.getSeats().size() : 0)
                              .append("\n");
                    }
                }
            } catch (Exception e) {
                result.append("Error getting bookings: ").append(e.getMessage()).append("\n");
            }
            
            result.append("\n=== SCHEDULES ===\n");
            try {
                List<com.trainmanagement.trainmanagementsystem.entity.TrainSchedule> schedules = 
                    trainScheduleService.getAllSchedules();
                result.append("Total schedules: ").append(schedules.size()).append("\n");
                for (com.trainmanagement.trainmanagementsystem.entity.TrainSchedule schedule : schedules) {
                    result.append("Schedule ID: ").append(schedule.getScheduleId())
                          .append(", From: ").append(schedule.getFromStation())
                          .append(", To: ").append(schedule.getToStation())
                          .append(", Train ID: ").append(schedule.getTrain() != null ? schedule.getTrain().getId() : "null")
                          .append("\n");
                }
            } catch (Exception e) {
                result.append("Error getting schedules: ").append(e.getMessage()).append("\n");
            }
            
            result.append("\n=== SEATS ===\n");
            try {
                // Check seats for first few schedules
                for (int i = 1; i <= 3; i++) {
                    List<Seat> seats = seatRepository.findByScheduleIdOrderByCoachAndSeat((long) i);
                    result.append("Schedule ").append(i).append(" seats: ").append(seats.size()).append("\n");
                    if (seats.size() > 0) {
                        result.append("  - Available: ").append(seats.stream().mapToInt(s -> s.isAvailable() ? 1 : 0).sum())
                              .append(", Occupied: ").append(seats.stream().mapToInt(s -> s.isAvailable() ? 0 : 1).sum())
                              .append("\n");
                    }
                }
            } catch (Exception e) {
                result.append("Error getting seats: ").append(e.getMessage()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }

    // Simple test endpoint to check if booking 11 exists
    @GetMapping("/debug/check-booking-11")
    @ResponseBody
    public String checkBooking11() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== CHECKING BOOKING 11 ===\n");
            
            Booking booking = bookingService.findById(11L);
            if (booking == null) {
                result.append("Booking 11 does not exist!\n");
                result.append("Available bookings:\n");
                
                // Check for any bookings
                String[] testPassengers = {"admin", "john", "sarah", "test"};
                for (String passengerName : testPassengers) {
                    List<Booking> bookings = bookingService.getBookingsByPassengerName(passengerName);
                    result.append("Bookings for '").append(passengerName).append("': ").append(bookings.size()).append("\n");
                    for (Booking b : bookings) {
                        result.append("  - Booking ID: ").append(b.getId())
                              .append(", Status: ").append(b.getStatus())
                              .append(", Passenger: ").append(b.getPassengerName())
                              .append("\n");
                    }
                }
            } else {
                result.append("Booking 11 found!\n");
                result.append("ID: ").append(booking.getId()).append("\n");
                result.append("Status: ").append(booking.getStatus()).append("\n");
                result.append("Passenger: ").append(booking.getPassengerName()).append("\n");
                result.append("Schedule ID: ").append(booking.getSchedule() != null ? booking.getSchedule().getScheduleId() : "null").append("\n");
                result.append("Seats: ").append(booking.getSeats() != null ? booking.getSeats().size() : 0).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }

    // Debug endpoint to adjust booking time for testing
    @GetMapping("/debug/adjust-booking-time/{bookingId}")
    @ResponseBody
    public String adjustBookingTime(@PathVariable Long bookingId, 
                                   @RequestParam(defaultValue = "35") int minutesAgo) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== ADJUSTING BOOKING TIME ===\n");
            
            Booking booking = bookingService.findById(bookingId);
            if (booking == null) {
                return "Booking with ID " + bookingId + " not found!\n";
            }
            
            // Get current time and subtract the specified minutes
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime newBookingTime = now.minusMinutes(minutesAgo);
            
            result.append("Original booking time: ").append(booking.getBookingTime()).append("\n");
            result.append("New booking time: ").append(newBookingTime).append("\n");
            result.append("Minutes ago: ").append(minutesAgo).append("\n");
            
            // Update the booking time in the database
            booking.setBookingTime(newBookingTime);
            bookingService.updateBooking(booking);
            
            // Check the modification status
            String status = bookingService.getBookingModificationStatus(bookingId);
            result.append("Modification status: ").append(status).append("\n");
            
            if (minutesAgo > 30) {
                result.append("✅ Booking is now ").append(minutesAgo).append(" minutes old - seat changes should be blocked!\n");
                result.append("Test URL: http://localhost:8080/passenger/bookings/").append(bookingId).append("/change-seats\n");
            } else {
                result.append("✅ Booking is now ").append(minutesAgo).append(" minutes old - seat changes should be allowed!\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }

    // Create test booking data
    @GetMapping("/debug/create-test-booking")
    @ResponseBody
    public String createTestBooking() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== CREATING TEST BOOKING ===\n");
            
            // Get first available schedule
            List<com.trainmanagement.trainmanagementsystem.entity.TrainSchedule> schedules = 
                trainScheduleService.getAllSchedules();
            
            if (schedules.isEmpty()) {
                return "No schedules found. Please create schedules first.\n";
            }
            
            com.trainmanagement.trainmanagementsystem.entity.TrainSchedule schedule = schedules.get(0);
            result.append("Using schedule: ").append(schedule.getScheduleId()).append("\n");
            
            // Get available seats for this schedule
            List<Seat> availableSeats = seatRepository.findByScheduleIdOrderByCoachAndSeat(schedule.getScheduleId())
                    .stream()
                    .filter(Seat::isAvailable)
                    .limit(2) // Take first 2 available seats
                    .collect(java.util.stream.Collectors.toList());
            
            if (availableSeats.isEmpty()) {
                return "No available seats found for schedule " + schedule.getScheduleId() + "\n";
            }
            
            result.append("Found ").append(availableSeats.size()).append(" available seats\n");
            
            // Create a test booking
            com.trainmanagement.trainmanagementsystem.dto.BookingRequest bookingRequest = 
                new com.trainmanagement.trainmanagementsystem.dto.BookingRequest();
            bookingRequest.setScheduleId(schedule.getScheduleId());
            bookingRequest.setPassengerName("john");
            bookingRequest.setSeatIds(availableSeats.stream().map(Seat::getId).collect(java.util.stream.Collectors.toList()));
            
            Booking booking = bookingService.bookTickets(bookingRequest);
            result.append("Created booking ID: ").append(booking.getId()).append("\n");
            result.append("Seats: ").append(booking.getSeats().stream().map(Seat::getSeatNumber).collect(java.util.stream.Collectors.joining(", "))).append("\n");
            
            return result.toString();
        } catch (Exception e) {
            return "Error creating test booking: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }
    
    // Download ticket PDF
    @GetMapping("/bookings/{id}/download-ticket")
    @ResponseBody
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long id) {
        try {
            // Get current user authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("[DEBUG] DownloadTicket - Principal type: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
            System.out.println("[DEBUG] DownloadTicket - Principal value: " + principal);
            
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                System.out.println("[DEBUG] DownloadTicket - User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Get current user
            String username = (String) principal;
            System.out.println("[DEBUG] DownloadTicket - Username: " + username);
            Passenger currentUser = passengerService.findByUsername(username).orElse(null);
            
            if (currentUser == null) {
                System.out.println("[DEBUG] DownloadTicket - Passenger not found for username: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            System.out.println("[DEBUG] DownloadTicket - Current user: " + currentUser.getUsername() + " (" + currentUser.getFullName() + ")");

            // Find the booking
            Booking booking = bookingService.findById(id);
            if (booking == null) {
                System.out.println("[DEBUG] DownloadTicket - Booking not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            System.out.println("[DEBUG] DownloadTicket - Found booking: " + booking.getId() + 
                             ", Passenger: " + booking.getPassengerName() + 
                             ", Status: " + booking.getStatus());

            // Check if the booking belongs to the current user
            // Try to match by username first, then by full name
            boolean isOwner = booking.getPassengerName().equals(currentUser.getUsername()) ||
                             booking.getPassengerName().equals(currentUser.getFullName());
            
            if (!isOwner) {
                System.out.println("[DEBUG] DownloadTicket - Access denied. Booking passenger: " + booking.getPassengerName() + 
                                 ", Current user: " + currentUser.getUsername() + " (" + currentUser.getFullName() + ")");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if booking has ticket generated by ticket officer
            if (!"TICKET_GENERATED".equals(booking.getStatus())) {
                System.out.println("[DEBUG] DownloadTicket - Ticket not generated yet. Status: " + booking.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Generate PDF ticket
            byte[] pdfBytes = ticketPdfService.generateTicketPdf(booking);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "train-ticket-" + booking.getId() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            System.err.println("[ERROR] PassengerController - Error generating ticket PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("[ERROR] PassengerController - Error downloading ticket: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
