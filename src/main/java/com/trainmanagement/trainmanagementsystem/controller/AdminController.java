package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.PassengerFeedback;
import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.entity.Train;
import com.trainmanagement.trainmanagementsystem.service.PassengerFeedbackService;
import com.trainmanagement.trainmanagementsystem.service.AlertService;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.service.TrainScheduleService;
import com.trainmanagement.trainmanagementsystem.repository.TrainRepository;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PassengerFeedbackService feedbackService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private TrainScheduleService trainScheduleService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private RoleBasedAccessControl rbac;

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff and train station masters can access dashboard
        if (!rbac.canAccessAdminDashboard()) {
            return "redirect:/access-denied";
        }

        // Get statistics for admin dashboard
        List<PassengerFeedback> allFeedback = feedbackService.findAll();
        List<Alert> allAlerts = alertService.findAllSorted();
        List<TrainSchedule> allSchedules = trainScheduleService.getAllSchedules();
        
        long newFeedbackCount = allFeedback.stream()
                .filter(f -> "New".equals(f.getStatus()))
                .count();
        
        long inReviewFeedbackCount = allFeedback.stream()
                .filter(f -> "In Review".equals(f.getStatus()))
                .count();
        
        long resolvedFeedbackCount = allFeedback.stream()
                .filter(f -> "Resolved".equals(f.getStatus()))
                .count();

        // Schedule statistics
        long onTimeSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == TrainSchedule.ScheduleStatus.ON_TIME)
                .count();
        
        long delayedSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == TrainSchedule.ScheduleStatus.DELAYED)
                .count();
        
        long cancelledSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == TrainSchedule.ScheduleStatus.CANCELLED)
                .count();

        model.addAttribute("totalFeedback", allFeedback.size());
        model.addAttribute("newFeedbackCount", newFeedbackCount);
        model.addAttribute("inReviewFeedbackCount", inReviewFeedbackCount);
        model.addAttribute("resolvedFeedbackCount", resolvedFeedbackCount);
        model.addAttribute("totalAlerts", allAlerts.size());
        model.addAttribute("totalSchedules", allSchedules.size());
        model.addAttribute("onTimeSchedules", onTimeSchedules);
        model.addAttribute("delayedSchedules", delayedSchedules);
        model.addAttribute("cancelledSchedules", cancelledSchedules);
        model.addAttribute("recentFeedback", allFeedback.stream().limit(5).toList());
        model.addAttribute("recentAlerts", allAlerts.stream().limit(3).toList());
        model.addAttribute("recentSchedules", allSchedules.stream().limit(5).toList());
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

        return "admin/dashboard";
    }

    // Feedback Management
    @GetMapping("/feedback")
    public String manageFeedback(Model model) {
        // Check authentication and feedback access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Only passenger experience analysts and admin staff can access feedback
        if (!rbac.canAccessFeedbackManagement()) {
            return "redirect:/access-denied";
        }

        List<PassengerFeedback> feedbackList = feedbackService.findAll();
        model.addAttribute("feedbackList", feedbackList);
        
        // Add user role information
        Passenger currentUser = rbac.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/feedback-list";
    }

    @PostMapping("/feedback/update/{id}")
    public String updateFeedback(@PathVariable Long id, 
                                @RequestParam String status,
                                @RequestParam(required = false) String response,
                                RedirectAttributes redirectAttributes) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            Passenger analyst = passengerService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            feedbackService.updateStatus(id, status, response, analyst);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating feedback: " + e.getMessage());
        }
        
        return "redirect:/admin/feedback";
    }

    @PostMapping("/feedback/delete/{id}")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting feedback: " + e.getMessage());
        }
        
        return "redirect:/admin/feedback";
    }

    // Train Station Master Dashboard
    @GetMapping("/station-master/dashboard")
    public String stationMasterDashboard(Model model) {
        // Check authentication and station master access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only train station masters can access this dashboard
        if (currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
            return "redirect:/access-denied";
        }

        // Get statistics for station master dashboard
        List<Alert> allAlerts = alertService.findAllSorted();
        
        model.addAttribute("totalAlerts", allAlerts.size());
        model.addAttribute("recentAlerts", allAlerts.stream().limit(5).toList());
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

        return "admin/station-master-dashboard";
    }

    // Passenger Experience Analyst Dashboard
    @GetMapping("/analyst/dashboard")
    public String analystDashboard(Model model) {
        // Check authentication and analyst access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only passenger experience analysts can access this dashboard
        if (currentUser.getRole() != Passenger.UserRole.PASSENGER_EXPERIENCE_ANALYST) {
            return "redirect:/access-denied";
        }

        // Get statistics for analyst dashboard
        List<PassengerFeedback> allFeedback = feedbackService.findAll();
        
        long newFeedbackCount = allFeedback.stream()
                .filter(f -> "New".equals(f.getStatus()))
                .count();
        
        long inReviewFeedbackCount = allFeedback.stream()
                .filter(f -> "In Review".equals(f.getStatus()))
                .count();
        
        long resolvedFeedbackCount = allFeedback.stream()
                .filter(f -> "Resolved".equals(f.getStatus()))
                .count();
        
        model.addAttribute("totalFeedback", allFeedback.size());
        model.addAttribute("newFeedbackCount", newFeedbackCount);
        model.addAttribute("inReviewFeedbackCount", inReviewFeedbackCount);
        model.addAttribute("resolvedFeedbackCount", resolvedFeedbackCount);
        model.addAttribute("recentFeedback", allFeedback.stream().limit(5).toList());
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

        return "admin/analyst-dashboard";
    }

    // Alert Management
    @GetMapping("/alerts")
    public String manageAlerts(Model model) {
        // Check authentication and alert access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Only train station masters and admin staff can access alerts
        if (!rbac.canAccessAlertManagement()) {
            return "redirect:/access-denied";
        }

        List<Alert> alertList = alertService.findAllSorted();
        model.addAttribute("alertList", alertList);
        
        // Add user role information
        Passenger currentUser = rbac.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/alert-list";
    }

    @GetMapping("/alerts/new")
    public String showAlertForm(Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        model.addAttribute("alert", new Alert());
        return "admin/alert-form";
    }

    @PostMapping("/alerts/create")
    public String createAlert(@ModelAttribute("alert") Alert alert, RedirectAttributes redirectAttributes) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            Passenger admin = passengerService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            alertService.createAlert(alert, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Alert created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating alert: " + e.getMessage());
        }
        
        return "redirect:/admin/alerts";
    }

    // Quick Alert Templates
    @GetMapping("/alerts/delay")
    public String showDelayAlertForm(Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        Alert alert = new Alert();
        alert.setMessage("🚨 DELAY NOTICE: Due to [reason], Train #[train_number] will be delayed by approximately [duration] minutes. We apologize for any inconvenience caused. Please check for updates.");
        model.addAttribute("alert", alert);
        model.addAttribute("alertType", "delay");
        return "admin/alert-form";
    }

    @GetMapping("/alerts/cancellation")
    public String showCancellationAlertForm(Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        Alert alert = new Alert();
        alert.setMessage("❌ CANCELLATION NOTICE: Train #[train_number] scheduled for [date] at [time] has been cancelled due to [reason]. Affected passengers will be notified about alternative arrangements. We sincerely apologize for the inconvenience.");
        model.addAttribute("alert", alert);
        model.addAttribute("alertType", "cancellation");
        return "admin/alert-form";
    }

    @GetMapping("/alerts/maintenance")
    public String showMaintenanceAlertForm(Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        Alert alert = new Alert();
        alert.setMessage("🔧 MAINTENANCE NOTICE: Scheduled maintenance work will affect train services on [date] from [start_time] to [end_time]. Some trains may experience delays. We appreciate your patience during this period.");
        model.addAttribute("alert", alert);
        model.addAttribute("alertType", "maintenance");
        return "admin/alert-form";
    }

    @PostMapping("/alerts/delete/{id}")
    public String deleteAlert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        try {
            alertService.deleteAlert(id);
            redirectAttributes.addFlashAttribute("successMessage", "Alert deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting alert: " + e.getMessage());
        }
        
        return "redirect:/admin/alerts";
    }

    // Train Management
    @GetMapping("/trains")
    public String manageTrains(Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff and train station masters can access train management
        if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
            return "redirect:/access-denied";
        }

        List<Train> trainList = trainRepository.findAll();
        model.addAttribute("trainList", trainList);
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/train-list";
    }

    @GetMapping("/trains/new")
    public String showTrainForm(Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff and train station masters can create trains
        if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
            return "redirect:/access-denied";
        }

        model.addAttribute("train", new Train());
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/train-form";
    }

    @GetMapping("/trains/edit/{id}")
    public String editTrain(@PathVariable Long id, Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff and train station masters can edit trains
        if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
            return "redirect:/access-denied";
        }

        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        model.addAttribute("train", train);
        
        // Add user role information
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/train-form";
    }

    @PostMapping("/trains/save")
    public String saveTrain(@ModelAttribute("train") Train train, RedirectAttributes redirectAttributes) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff and train station masters can save trains
        if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
            return "redirect:/access-denied";
        }
        
        try {
            trainRepository.save(train);
            if (train.getId() == null) {
                redirectAttributes.addFlashAttribute("successMessage", "Train created successfully!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Train updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving train: " + e.getMessage());
        }
        
        return "redirect:/admin/trains";
    }

    @PostMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff can delete trains
        if (!rbac.hasAdminStaffAccess()) {
            return "redirect:/access-denied";
        }
        
        try {
            Train train = trainRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
            
            // Check if train has schedules
            if (train.getSchedules() != null && !train.getSchedules().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Cannot delete train. It has associated schedules. Please delete all schedules first.");
            } else {
                trainRepository.delete(train);
                redirectAttributes.addFlashAttribute("successMessage", "Train deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting train: " + e.getMessage());
        }
        
        return "redirect:/admin/trains";
    }

    // User Management Methods
    @GetMapping("/users")
    public String manageUsers(Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff can manage users
        if (!rbac.hasAdminStaffAccess()) {
            return "redirect:/access-denied";
        }

        List<Passenger> users = passengerService.getAllPassengers();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRoles", Passenger.UserRole.values());
        
        return "admin/user-list";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        // Check authentication and admin access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Only admin staff can edit users
        if (!rbac.hasAdminStaffAccess()) {
            return "redirect:/access-denied";
        }

        Passenger user = passengerService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRoles", Passenger.UserRole.values());
        
        return "admin/user-form";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, 
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String username,
                           @RequestParam Passenger.UserRole role,
                           RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and admin access
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Only admin staff can update users
            if (!rbac.hasAdminStaffAccess()) {
                return "redirect:/access-denied";
            }

            // Prevent admin from changing their own role
            if (currentUser.getId().equals(id) && !role.equals(currentUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "You cannot change your own role");
                return "redirect:/admin/users/edit/" + id;
            }

            passengerService.updateUserInfo(id, fullName, email, phone, username);
            passengerService.updateUserRole(id, role);
            
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    @PostMapping("/users/change-password/{id}")
    public String changeUserPassword(@PathVariable Long id,
                                   @RequestParam String newPassword,
                                   @RequestParam String confirmPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and admin access
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Only admin staff can change passwords
            if (!rbac.hasAdminStaffAccess()) {
                return "redirect:/access-denied";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/admin/users/edit/" + id;
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long");
                return "redirect:/admin/users/edit/" + id;
            }

            passengerService.updateUserPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
            return "redirect:/admin/users/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and admin access
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Only admin staff can delete users
            if (!rbac.hasAdminStaffAccess()) {
                return "redirect:/access-denied";
            }

            // Prevent admin from deleting themselves
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account");
                return "redirect:/admin/users";
            }

            passengerService.deletePassenger(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
