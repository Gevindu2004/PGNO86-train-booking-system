package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.entity.Pricing;
import com.trainmanagement.trainmanagementsystem.service.PricingService;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/ticket-officer")
public class PricingController {

    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private RoleBasedAccessControl rbac;

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
            
            model.addAttribute("pricingList", allPricing);
            model.addAttribute("allStations", allStations);
            model.addAttribute("allRoutes", allRoutes);
            model.addAttribute("classTypes", Pricing.ClassType.values());
            
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
                return "redirect:/ticket-officer/pricing/add";
            }

            // Check if pricing already exists
            if (pricingService.pricingExists(pricing.getFromStation(), pricing.getToStation(), 
                    pricing.getClassType().name())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Pricing already exists for " + pricing.getFromStation() + " to " + 
                    pricing.getToStation() + " in " + pricingService.getClassName(pricing.getClassType().name()) + "!");
                return "redirect:/ticket-officer/pricing/add";
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
    @GetMapping("/edit/{id}")
    public String showEditPricingForm(@PathVariable("id") Long id, Model model) {
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

            var pricingOpt = pricingService.getPricingById(id);
            if (pricingOpt.isEmpty()) {
                return "redirect:/ticket-officer/pricing?error=Pricing not found";
            }

            List<String> allStations = pricingService.getAllStations();
            
            model.addAttribute("pricing", pricingOpt.get());
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

    // Process form to update existing pricing
    @PostMapping("/edit/{id}")
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
}
