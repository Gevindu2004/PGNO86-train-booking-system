package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.entity.Train;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.service.TrainScheduleService;
import com.trainmanagement.trainmanagementsystem.repository.TrainRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/schedules")
public class TrainScheduleController {

    @Autowired
    private TrainScheduleService trainScheduleService;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoleBasedAccessControl rbac;

    // List all schedules for admin
    @GetMapping
    public String listSchedules(Model model) {
        // Check authentication and schedule management access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Only admin staff can access schedule management
        if (!rbac.canAccessScheduleManagement()) {
            return "redirect:/access-denied";
        }

        List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        
        // Check for orphaned seats (seats with null schedule_id)
        List<Seat> orphanedSeats = seatRepository.findByScheduleIdIsNull();
        if (!orphanedSeats.isEmpty()) {
            model.addAttribute("warningMessage", 
                String.format("Warning: Found %d orphaned seats (with null schedule_id) in the database. These seats are not associated with any schedule and cannot be used for bookings. Use the 'Clean Up Orphaned Seats' button to remove them.", 
                    orphanedSeats.size()));
        }
        
        // Add user role information
        Passenger currentUser = rbac.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userRole", currentUser.getRole());
        model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
        
        return "admin/schedule-list";
    }

    // Show create form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        TrainSchedule schedule = new TrainSchedule();
        // No longer need to pass trains list since we're using text input
        model.addAttribute("schedule", schedule);
        model.addAttribute("isEdit", false);
        return "admin/schedule-form";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
        TrainSchedule schedule = schedules.stream()
                .filter(s -> s.getScheduleId().equals(id))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            return "redirect:/admin/schedules";
        }

        // Get the train name for the text input
        String trainName = "";
        if (schedule.getTrainId() != null) {
            Train train = trainRepository.findById(schedule.getTrainId()).orElse(null);
            if (train != null) {
                trainName = train.getName();
            }
        }
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("trainName", trainName);
        model.addAttribute("isEdit", true);
        return "admin/schedule-form";
    }

    // Save schedule (create or update)
    @PostMapping("/save")
    public String saveSchedule(@ModelAttribute("schedule") TrainSchedule schedule, 
                              @RequestParam(value = "isEdit", defaultValue = "false") boolean isEdit,
                              @RequestParam(value = "trainName", required = false) String trainName,
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
            // Handle train name input
            Train train = null;
            if (trainName != null && !trainName.trim().isEmpty()) {
                // Try to find existing train by name
                List<Train> existingTrains = trainRepository.findAll();
                train = existingTrains.stream()
                        .filter(t -> t.getName().equalsIgnoreCase(trainName.trim()))
                        .findFirst()
                        .orElse(null);
                
                // If train doesn't exist, create a new one
                if (train == null) {
                    train = new Train();
                    train.setName(trainName.trim());
                    train.setRoute(schedule.getFromStation() + " - " + schedule.getToStation());
                    train = trainRepository.save(train);
                }
                
                // Set the train ID in the schedule
                schedule.setTrainId(train.getId());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Train name is required.");
                return "redirect:/admin/schedules/new";
            }
            
            if (isEdit) {
                trainScheduleService.updateSchedule(schedule.getScheduleId(), schedule);
                redirectAttributes.addFlashAttribute("successMessage", "Schedule updated successfully!");
            } else {
                trainScheduleService.createSchedule(schedule);
                redirectAttributes.addFlashAttribute("successMessage", "Schedule created successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving schedule: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }

    // Delete schedule
    @PostMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
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
            trainScheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting schedule: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }

    // Update schedule status
    @PostMapping("/update-status/{id}")
    public String updateScheduleStatus(@PathVariable("id") Long id, 
                                     @RequestParam("status") String status,
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
            List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
            TrainSchedule schedule = schedules.stream()
                    .filter(s -> s.getScheduleId().equals(id))
                    .findFirst()
                    .orElse(null);

            if (schedule != null) {
                schedule.setStatusString(status);
                trainScheduleService.updateSchedule(id, schedule);
                redirectAttributes.addFlashAttribute("successMessage", "Schedule status updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Schedule not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating schedule status: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }
}
