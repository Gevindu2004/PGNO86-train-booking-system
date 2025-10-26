package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.entity.Pricing;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.service.PricingService;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.service.BookingService;
import com.trainmanagement.trainmanagementsystem.service.AlertService;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/ticket-officer")
public class PricingController {

    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private RoleBasedAccessControl rbac;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PassengerService passengerService;
    
    @Autowired
    private AlertService alertService;

    // Ticket Officer Dashboard
    @GetMapping("/dashboard")
    public String ticketOfficerDashboard(Model model) {
        try {
            System.out.println("=== TICKET OFFICER DASHBOARD ACCESSED ===");
            
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                System.out.println("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Current user is null, redirecting to login");
                return "redirect:/login";
            }
            
            System.out.println("Current user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                System.out.println("User does not have ticket officer access, redirecting to access denied");
                return "redirect:/access-denied";
            }
            
            System.out.println("User has ticket officer access, proceeding to dashboard");

            // Get statistics for ticket officer dashboard
            List<Pricing> allPricing = pricingService.getAllPricing();
            long totalPricingRecords = allPricing.size();
            long firstClassRoutes = allPricing.stream().filter(p -> p.getClassType() == Pricing.ClassType.A).count();
            long secondClassRoutes = allPricing.stream().filter(p -> p.getClassType() == Pricing.ClassType.B).count();
            long thirdClassRoutes = allPricing.stream().filter(p -> p.getClassType() == Pricing.ClassType.C).count();
            
            model.addAttribute("totalPricingRecords", totalPricingRecords);
            model.addAttribute("firstClassRoutes", firstClassRoutes);
            model.addAttribute("secondClassRoutes", secondClassRoutes);
            model.addAttribute("thirdClassRoutes", thirdClassRoutes);
            model.addAttribute("recentPricing", allPricing.stream().limit(5).toList());
            
            // Add user role information
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRole", currentUser.getRole());
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            return "ticket-officer/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/access-denied";
        }
    }

    // Show pricing management dashboard for ticket officers
    @GetMapping("/pricing")
    public String showPricingManagement(Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Get all pricing records
            List<Pricing> allPricing = pricingService.getAllPricing();
            List<String> allStations = pricingService.getAllStations();
            List<String> allRoutes = pricingService.getAllRoutes();
            
            // Calculate counts by class type
            long firstClassCount = allPricing.stream()
                    .filter(pricing -> pricing.getClassType() != null && pricing.getClassType().name().equals("A"))
                    .count();
            long secondClassCount = allPricing.stream()
                    .filter(pricing -> pricing.getClassType() != null && pricing.getClassType().name().equals("B"))
                    .count();
            long thirdClassCount = allPricing.stream()
                    .filter(pricing -> pricing.getClassType() != null && pricing.getClassType().name().equals("C"))
                    .count();
            
            model.addAttribute("pricingList", allPricing);
            model.addAttribute("allStations", allStations);
            model.addAttribute("allRoutes", allRoutes);
            model.addAttribute("classTypes", Pricing.ClassType.values());
            model.addAttribute("firstClassCount", firstClassCount);
            model.addAttribute("secondClassCount", secondClassCount);
            model.addAttribute("thirdClassCount", thirdClassCount);
            
            // Add user role information
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRole", currentUser.getRole());
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            return "ticket-officer/pricing-management";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/access-denied";
        }
    }

    // Show form to add new pricing
    @GetMapping("/add")
    public String showAddPricingForm(Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            List<String> allStations = pricingService.getAllStations();
            
            model.addAttribute("pricing", new Pricing());
            model.addAttribute("allStations", allStations);
            model.addAttribute("classTypes", Pricing.ClassType.values());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            return "ticket-officer/pricing-form";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/ticket-officer/pricing";
        }
    }

    // Process form to add new pricing
    @PostMapping("/add")
    public String addPricing(@ModelAttribute Pricing pricing, RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Validate input
            if (pricing.getFromStation() == null || pricing.getFromStation().trim().isEmpty() ||
                pricing.getToStation() == null || pricing.getToStation().trim().isEmpty() ||
                pricing.getClassType() == null || pricing.getPrice() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "All fields are required!");
                return "redirect:/ticket-officer/add";
            }

            // Check if pricing already exists
            if (pricingService.pricingExists(pricing.getFromStation(), pricing.getToStation(), 
                    pricing.getClassType().name())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Pricing already exists for " + pricing.getFromStation() + " to " + 
                    pricing.getToStation() + " in " + pricingService.getClassName(pricing.getClassType().name()) + "!");
                return "redirect:/ticket-officer/add";
            }

            // Save the pricing
            Pricing savedPricing = pricingService.savePricing(pricing);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully added pricing for " + savedPricing.getFromStation() + " to " + 
                savedPricing.getToStation() + " in " + pricingService.getClassName(savedPricing.getClassType().name()) + 
                " - Rs. " + savedPricing.getPrice());

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding pricing: " + e.getMessage());
        }

        return "redirect:/ticket-officer/pricing";
    }

    // Show form to edit existing pricing
    @GetMapping("/pricing/edit/{id}")
    public String showEditPricingForm(@PathVariable("id") Long id, Model model) {
        try {
            System.out.println("[DEBUG] PricingController - Edit form requested for ID: " + id);
            
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                System.out.println("[DEBUG] PricingController - User not authenticated");
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                System.out.println("[DEBUG] PricingController - Current user is null");
                return "redirect:/login";
            }
            
            System.out.println("[DEBUG] PricingController - Current user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                System.out.println("[DEBUG] PricingController - User does not have access");
                return "redirect:/access-denied";
            }

            System.out.println("[DEBUG] PricingController - Fetching pricing with ID: " + id);
            var pricingOpt = pricingService.getPricingById(id);
            if (pricingOpt.isEmpty()) {
                System.out.println("[DEBUG] PricingController - Pricing not found for ID: " + id);
                return "redirect:/ticket-officer/pricing?error=Pricing not found";
            }

            System.out.println("[DEBUG] PricingController - Pricing found: " + pricingOpt.get());
            List<String> allStations = pricingService.getAllStations();
            System.out.println("[DEBUG] PricingController - All stations: " + allStations);
            
            model.addAttribute("pricing", pricingOpt.get());
            model.addAttribute("allStations", allStations);
            model.addAttribute("classTypes", Pricing.ClassType.values());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            System.out.println("[DEBUG] PricingController - Returning template: ticket-officer/pricing-form");
            return "ticket-officer/pricing-form";
        } catch (Exception e) {
            System.out.println("[ERROR] PricingController - Exception in edit form: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/ticket-officer/pricing";
        }
    }

    // Process form to update existing pricing
    @PostMapping("/pricing/edit/{id}")
    public String updatePricing(@PathVariable("id") Long id, @ModelAttribute Pricing pricing, 
                               RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Validate input
            if (pricing.getFromStation() == null || pricing.getFromStation().trim().isEmpty() ||
                pricing.getToStation() == null || pricing.getToStation().trim().isEmpty() ||
                pricing.getClassType() == null || pricing.getPrice() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "All fields are required!");
                return "redirect:/ticket-officer/pricing/edit/" + id;
            }

            // Check if pricing exists
            var existingPricingOpt = pricingService.getPricingById(id);
            if (existingPricingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Pricing not found!");
                return "redirect:/ticket-officer/pricing";
            }

            // Update the pricing
            Pricing existingPricing = existingPricingOpt.get();
            existingPricing.setFromStation(pricing.getFromStation());
            existingPricing.setToStation(pricing.getToStation());
            existingPricing.setClassType(pricing.getClassType());
            existingPricing.setPrice(pricing.getPrice());
            
            Pricing updatedPricing = pricingService.savePricing(existingPricing);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully updated pricing for " + updatedPricing.getFromStation() + " to " + 
                updatedPricing.getToStation() + " in " + pricingService.getClassName(updatedPricing.getClassType().name()) + 
                " - Rs. " + updatedPricing.getPrice());

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating pricing: " + e.getMessage());
        }

        return "redirect:/ticket-officer/pricing";
    }

    // Delete pricing
    @PostMapping("/delete/{id}")
    public String deletePricing(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Check if pricing exists
            var pricingOpt = pricingService.getPricingById(id);
            if (pricingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Pricing not found!");
                return "redirect:/ticket-officer/pricing";
            }

            Pricing pricing = pricingOpt.get();
            String routeInfo = pricing.getFromStation() + " to " + pricing.getToStation() + 
                             " in " + pricingService.getClassName(pricing.getClassType().name());
            
            // Delete the pricing
            pricingService.deletePricing(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully deleted pricing for " + routeInfo);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting pricing: " + e.getMessage());
        }

        return "redirect:/ticket-officer/pricing";
    }

    // Quick add pricing for a specific route and class
    @PostMapping("/pricing/quick-add")
    public String quickAddPricing(@RequestParam("fromStation") String fromStation,
                                 @RequestParam("toStation") String toStation,
                                 @RequestParam("classType") String classType,
                                 @RequestParam("price") BigDecimal price,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== QUICK ADD PRICING REQUEST ===");
            System.out.println("From Station: " + fromStation);
            System.out.println("To Station: " + toStation);
            System.out.println("Class Type: " + classType);
            System.out.println("Price: " + price);
            
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                System.out.println("User not authenticated for quick add");
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Validate input
            if (fromStation == null || fromStation.trim().isEmpty() ||
                toStation == null || toStation.trim().isEmpty() ||
                classType == null || classType.trim().isEmpty() ||
                price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "All fields are required and price must be greater than 0!");
                return "redirect:/ticket-officer/pricing";
            }

            // Create or update pricing
            System.out.println("Creating/updating pricing...");
            Pricing savedPricing = pricingService.createOrUpdatePricing(fromStation, toStation, classType, price);
            System.out.println("Pricing saved with ID: " + savedPricing.getId());
            
            String successMessage = "Successfully " + (pricingService.pricingExists(fromStation, toStation, classType) ? "updated" : "added") + 
                " pricing for " + savedPricing.getFromStation() + " to " + 
                savedPricing.getToStation() + " in " + pricingService.getClassName(savedPricing.getClassType().name()) + 
                " - Rs. " + savedPricing.getPrice();
            
            System.out.println("Success message: " + successMessage);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding pricing: " + e.getMessage());
        }

        return "redirect:/ticket-officer/pricing";
    }

    // Test endpoint to verify ticket officer dashboard access
    @GetMapping("/test")
    public String testTicketOfficerAccess(Model model) {
        System.out.println("=== TICKET OFFICER TEST ENDPOINT ACCESSED ===");
        
        if (!rbac.isAuthenticated()) {
            System.out.println("User not authenticated");
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Current user is null");
            return "redirect:/login";
        }
        
        System.out.println("Test - Current user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
        
        model.addAttribute("message", "Ticket Officer Test - User: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
        return "ticket-officer/test";
    }

    // ===== BOOKING MANAGEMENT FOR TICKET OFFICERS =====

    // Show booking search page for ticket officers
    @GetMapping("/bookings")
    public String showBookingSearch(Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Get all bookings initially
            List<com.trainmanagement.trainmanagementsystem.entity.Booking> allBookings = bookingService.getAllBookings();
            
            model.addAttribute("bookings", allBookings);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
            model.addAttribute("searchUsername", "");
            model.addAttribute("searchDate", "");

            return "ticket-officer/booking-search";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/access-denied";
        }
    }

    // Search bookings by passenger username and/or date
    @PostMapping("/bookings/search")
    public String searchBookings(@RequestParam(required = false) String username, 
                                @RequestParam(required = false) java.time.LocalDate bookingDate, 
                                Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Search bookings by username and/or date
            List<com.trainmanagement.trainmanagementsystem.entity.Booking> bookings = bookingService.searchBookings(username, bookingDate);
            
            model.addAttribute("bookings", bookings);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
            model.addAttribute("searchUsername", username != null ? username : "");
            model.addAttribute("searchDate", bookingDate != null ? bookingDate.toString() : "");

            return "ticket-officer/booking-search";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/ticket-officer/bookings";
        }
    }

    // Show booking details for ticket officers
    @GetMapping("/bookings/{id}")
    public String showBookingDetails(@PathVariable Long id, Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Get booking details
            com.trainmanagement.trainmanagementsystem.entity.Booking booking = bookingService.findById(id);
            if (booking == null) {
                return "redirect:/ticket-officer/bookings?error=Booking not found";
            }
            
            model.addAttribute("booking", booking);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            return "ticket-officer/booking-details";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/ticket-officer/bookings?error=Error loading booking details";
        }
    }

    // Cancel booking (ticket officer action)
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has ticket officer access or admin access
            if (currentUser.getRole() != Passenger.UserRole.TICKET_OFFICER && 
                currentUser.getRole() != Passenger.UserRole.ADMIN_STAFF) {
                return "redirect:/access-denied";
            }

            // Cancel the booking
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully");
            
            return "redirect:/ticket-officer/bookings";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling booking: " + e.getMessage());
            return "redirect:/ticket-officer/bookings";
        }
    }
    
    // Booking Requests Management
    @GetMapping("/booking-requests")
    public String showBookingRequests(Model model) {
        try {
            // Get current user authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                return "redirect:/login?redirect=/ticket-officer/booking-requests";
            }

            // Get current user
            String username = (String) principal;
            Passenger currentUser = passengerService.findByUsername(username).orElse(null);
            
            if (currentUser == null) {
                return "redirect:/login?redirect=/ticket-officer/booking-requests";
            }

            // Check if user has access (using pricing management access as ticket officer access)
            if (!rbac.canAccessPricingManagement()) {
                return "redirect:/passenger/dashboard?error=Access denied";
            }

            // Get all pending bookings (bookings that need ticket generation)
            System.out.println("[DEBUG] PricingController - Getting pending bookings...");
            List<Booking> pendingBookings = bookingService.findAllPendingBookings();
            System.out.println("[DEBUG] PricingController - Found " + pendingBookings.size() + " pending bookings");
            
            // For now, create empty pricing map to avoid errors
            Map<Long, Map<String, Object>> bookingPricing = new HashMap<>();

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("bookingPricing", bookingPricing);
            
            return "ticket-officer/booking-requests";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading booking requests: " + e.getMessage());
            return "redirect:/ticket-officer/dashboard";
        }
    }
    
    @PostMapping("/booking-requests/{id}/confirm")
    public String confirmBookingRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get current user authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                return "redirect:/login?redirect=/ticket-officer/booking-requests";
            }

            // Get current user
            String username = (String) principal;
            Passenger currentUser = passengerService.findByUsername(username).orElse(null);
            
            if (currentUser == null) {
                return "redirect:/login?redirect=/ticket-officer/booking-requests";
            }

            // Check if user has access (using pricing management access as ticket officer access)
            if (!rbac.canAccessPricingManagement()) {
                return "redirect:/passenger/dashboard?error=Access denied";
            }

            // Find the booking
            Booking booking = bookingService.findById(id);
            if (booking == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
                return "redirect:/ticket-officer/booking-requests";
            }

            // Generate ticket and create alert for passenger
            booking.setStatus("TICKET_GENERATED");
            bookingService.saveBooking(booking);
            
            // Find the passenger to create alert
            // Try to find by username first, then by full name
            System.out.println("[DEBUG] PricingController - Looking for passenger with name: " + booking.getPassengerName());
            Passenger passenger = passengerService.findByUsername(booking.getPassengerName()).orElse(null);
            if (passenger == null) {
                // If not found by username, try to find by full name
                System.out.println("[DEBUG] PricingController - Not found by username, trying full name");
                passenger = passengerService.findByFullName(booking.getPassengerName()).orElse(null);
            }
            
            if (passenger != null) {
                System.out.println("[DEBUG] PricingController - Found passenger: " + passenger.getUsername());
                try {
                    // Create ticket alert for passenger
                    Alert ticketAlert = new Alert();
                    ticketAlert.setMessage(createTicketAlertMessage(booking));
                    ticketAlert.setPostedAt(LocalDateTime.now());
                    ticketAlert.setPostedBy(passenger); // Set the passenger as the recipient
                    
                    alertService.createAlert(ticketAlert, passenger);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        String.format("Booking #%d confirmed! Ticket generated and alert sent to %s", 
                            booking.getId(), passenger.getUsername()));
                } catch (Exception e) {
                    System.err.println("[ERROR] PricingController - Failed to create ticket alert: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("successMessage", 
                        String.format("Booking #%d confirmed and ticket generated! (Alert creation failed)", booking.getId()));
                }
            } else {
                System.out.println("[DEBUG] PricingController - Passenger not found for name: " + booking.getPassengerName());
                redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Booking #%d confirmed and ticket generated! (Passenger not found)", booking.getId()));
            }
            
            return "redirect:/ticket-officer/booking-requests";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error confirming booking: " + e.getMessage());
            return "redirect:/ticket-officer/booking-requests";
        }
    }
    
    private String createTicketAlertMessage(Booking booking) {
        StringBuilder message = new StringBuilder();
        
        message.append("🎫 Your train ticket has been confirmed!\n\n");
        message.append("Ticket #").append(booking.getId()).append("\n");
        message.append("Passenger: ").append(booking.getPassengerName()).append("\n");
        
        if (booking.getSchedule() != null) {
            if (booking.getSchedule().getTrain() != null) {
                message.append("Train: ").append(booking.getSchedule().getTrain().getName()).append("\n");
            }
            message.append("Route: ").append(booking.getSchedule().getFromStation())
                   .append(" → ").append(booking.getSchedule().getToStation()).append("\n");
            
            if (booking.getSchedule().getDate() != null) {
                message.append("Journey Date: ")
                       .append(booking.getSchedule().getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                       .append("\n");
            }
            
            if (booking.getSchedule().getDepartureTime() != null) {
                message.append("Departure Time: ").append(booking.getSchedule().getDepartureTime()).append("\n");
            }
        }
        
        if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
            message.append("Seats: ");
            for (int i = 0; i < booking.getSeats().size(); i++) {
                if (i > 0) message.append(", ");
                message.append(booking.getSeats().get(i).getSeatNumber());
            }
            message.append("\n");
        }
        
        message.append("\n✅ Your ticket is ready for download from your dashboard!\n");
        message.append("📱 You can download your PDF ticket anytime from 'My Bookings' page.\n");
        message.append("🚂 Please arrive at the station 30 minutes before departure.\n");
        message.append("🆔 Don't forget to bring a valid ID for verification.\n\n");
        message.append("Thank you for choosing Ceylon RailEase! Safe travels! 🚆");
        
        return message.toString();
    }
}
