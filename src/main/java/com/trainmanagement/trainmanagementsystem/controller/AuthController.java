package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private RoleBasedAccessControl rbac;

    @GetMapping("/login")
    public String showLoginForm(Model model, 
                               @RequestParam(required = false) String redirect,
                               @RequestParam(required = false) String error) {
        
        // Check if user is already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            
            // User is already logged in, redirect to appropriate dashboard
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("User " + username + " is already logged in, redirecting to dashboard");
            
            // Check if user can access admin dashboard
            if (rbac.canAccessAdminDashboard()) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/passenger/dashboard";
            }
        }
        
        // Pass redirect URL to the login form
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirectUrl", redirect);
        }
        // Pass error message if present
        if (error != null && "true".equals(error)) {
            model.addAttribute("error", "Invalid credentials. Please try again.");
        }
        return "login";
    }

    @PostMapping("/login/passenger")
    public String loginPassenger(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String redirectUrl,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        try {
            System.out.println("Login attempt for username: " + username);
            boolean result = passengerService.authenticate(username, password);
            System.out.println("Authentication result: " + result);
            Passenger passenger = passengerService.findByUsername(username)
                    .orElseGet(() -> passengerService.findByEmail(username).orElse(null));
            if (result && passenger != null) {
                System.out.println("Login successful for: " + passenger.getUsername());
                // Set authentication in Spring Security context with username as principal
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        passenger.getUsername(), null,
                        java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                // Store security context in session
                HttpSession session = request.getSession(true);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                                   SecurityContextHolder.getContext());
                
                System.out.println("Authentication stored in session for user: " + passenger.getUsername());
                
                // Redirect to the original URL if provided, otherwise check user type
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    System.out.println("Redirecting to: " + redirectUrl);
                    return "redirect:" + redirectUrl;
                } else {
                    // Role-based redirect logic
                    switch (passenger.getRole()) {
                        case ADMIN_STAFF:
                            System.out.println("Admin staff detected, redirecting to admin dashboard");
                            return "redirect:/admin/dashboard";
                        case TRAIN_STATION_MASTER:
                            System.out.println("Train Station Master detected, redirecting to admin dashboard");
                            return "redirect:/admin/dashboard";
                        case PASSENGER_EXPERIENCE_ANALYST:
                            System.out.println("Passenger Experience Analyst detected, redirecting to analyst dashboard");
                            return "redirect:/admin/analyst/dashboard";
                        case TICKET_OFFICER:
                            System.out.println("=== TICKET OFFICER LOGIN DETECTED ===");
                            System.out.println("Username: " + passenger.getUsername());
                            System.out.println("Role: " + passenger.getRole());
                            System.out.println("Redirecting to ticket officer dashboard");
                            return "redirect:/ticket-officer/dashboard";
                        case PASSENGER:
                        default:
                            // Check if there's pending booking data in session
                            HttpSession currentSession = request.getSession(false);
                            if (currentSession != null) {
                                // Check for booking-related session data
                                Object pendingBookingRequest = currentSession.getAttribute("pendingBookingRequest");
                                Object selectedSeats = currentSession.getAttribute("selectedSeats");
                                
                                if (pendingBookingRequest != null || selectedSeats != null) {
                                    System.out.println("Pending booking data found, redirecting to booking completion");
                                    return "redirect:/bookings/complete";
                                }
                            }
                            
                            System.out.println("No pending booking data, redirecting to passenger dashboard");
                            return "redirect:/passenger/dashboard";
                    }
                }
            } else {
                System.out.println("Login failed for: " + username);
                redirectAttributes.addFlashAttribute("error", "Invalid credentials");
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    return "redirect:/login?error=true&redirect=" + redirectUrl;
                } else {
                    return "redirect:/login?error=true";
                }
            }
        } catch (Exception e) {
            System.out.println("Exception during login for " + username + ": " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:/login?error=true&redirect=" + redirectUrl;
            } else {
                return "redirect:/login?error=true";
            }
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        System.out.println("Registration form requested");
        return "register";
    }

    @PostMapping("/register/passenger")
    public String registerPassenger(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Basic validation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "Passwords do not match");
            return "redirect:/register";
        }

        // Password validation
        if (password.length() < 8) {
            redirectAttributes.addAttribute("error", "Password must be at least 8 characters long");
            return "redirect:/register";
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            redirectAttributes.addAttribute("error", "Password must contain at least one symbol (!@#$%^&*)");
            return "redirect:/register";
        }

        // Additional validations
        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Full name is required");
            return "redirect:/register";
        }

        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Email is required");
            return "redirect:/register";
        }

        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Username is required");
            return "redirect:/register";
        }

        // Phone number validation
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "Phone number is required");
            return "redirect:/register";
        }
        
        // Remove any non-digit characters and check length
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() != 10) {
            redirectAttributes.addAttribute("error", "Phone number must be exactly 10 digits");
            return "redirect:/register";
        }

        try {
            System.out.println("Registration attempt for: " + username + " (" + email + ")");
            // Register new passenger with cleaned phone number
            passengerService.registerPassenger(fullName.trim(), email.trim(), cleanPhone, username.trim(), password);
            System.out.println("Registration successful for: " + username);
            redirectAttributes.addAttribute("success", "Registration successful! You can now login.");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Registration failed for " + username + ": " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Clear the security context
            SecurityContextHolder.clearContext();
            
            // Invalidate the session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            System.out.println("User logged out successfully");
            redirectAttributes.addFlashAttribute("logoutSuccess", "You have been successfully logged out!");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Logout failed. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/debug-user-role")
    public String debugUserRole() {
        Passenger user = rbac.getCurrentUser();
        if (user != null) {
            return "User: " + user.getUsername() + ", Role: " + user.getRole() + ", Role Name: " + user.getRole().name();
        } else {
            return "No authenticated user found";
        }
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        try {
            // Test if we can count passengers
            long passengerCount = passengerService.countPassengers();
            System.out.println("Database test successful. Passenger count: " + passengerCount);
            return "Database connection working. Passenger count: " + passengerCount;
        } catch (Exception e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
            return "Database test failed: " + e.getMessage();
        }
    }
}
