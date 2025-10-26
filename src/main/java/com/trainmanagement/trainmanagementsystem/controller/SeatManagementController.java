package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.TrainSchedule;
import com.trainmanagement.trainmanagementsystem.entity.Seat;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.TrainScheduleService;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/seats")
public class SeatManagementController {

    @Autowired
    private TrainScheduleService trainScheduleService;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private RoleBasedAccessControl rbac;

    // Show seat management for a specific schedule
    @GetMapping("/schedule/{scheduleId}")
    public String showSeatManagement(@PathVariable("scheduleId") Long scheduleId, Model model) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has admin access or is train station master
            if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
                return "redirect:/access-denied";
            }

            // Get schedule details
            List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
            TrainSchedule schedule = schedules.stream()
                    .filter(s -> s.getScheduleId().equals(scheduleId))
                    .findFirst()
                    .orElse(null);

            if (schedule == null) {
                model.addAttribute("errorMessage", "Schedule with ID " + scheduleId + " not found!");
                return "redirect:/admin/schedules?error=Schedule not found";
            }

            // Get seats for this schedule
            List<Seat> seats = seatRepository.findByScheduleIdOrderByCoachAndSeat(scheduleId);
            
            // Group seats by coach
            List<String> coaches = seats.stream()
                    .map(Seat::getCoachNum)
                    .filter(coachNum -> coachNum != null && !coachNum.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            // Add a message if no seats are found
            if (seats.isEmpty()) {
                model.addAttribute("infoMessage", "No seats have been generated for this schedule yet. Use the 'Generate Seats' button to create 300 seats across 15 coaches (A1-A5, B1-B5, C1-C5).");
            }
            
            // Check for orphaned seats (seats with null schedule_id)
            List<Seat> orphanedSeats = seatRepository.findByScheduleIdIsNull();
            if (!orphanedSeats.isEmpty()) {
                model.addAttribute("warningMessage", 
                    String.format("Warning: Found %d orphaned seats (with null schedule_id) in the database. These seats are not associated with any schedule and cannot be used for bookings. Use the 'Clean Up Orphaned Seats' button to remove them.", 
                        orphanedSeats.size()));
            }

            model.addAttribute("schedule", schedule);
            model.addAttribute("seats", seats);
            model.addAttribute("coaches", coaches);
            model.addAttribute("totalSeats", seats.size());
            model.addAttribute("availableSeats", seats.stream().mapToInt(s -> s.isAvailable() ? 1 : 0).sum());
            model.addAttribute("occupiedSeats", seats.stream().mapToInt(s -> s.isAvailable() ? 0 : 1).sum());
            
            // Add user role information
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userRole", currentUser.getRole());
            model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));

            return "admin/seat-management";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/schedules?error=Failed to load seat management";
        }
    }

    // Generate seats for a schedule (simple method with default parameters)
    @PostMapping("/generate/{scheduleId}")
    @Transactional
    public String generateSeats(@PathVariable("scheduleId") Long scheduleId,
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
            
            // Check if user has admin access or is train station master
            if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
                return "redirect:/access-denied";
            }

            // Get schedule details
            List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
            TrainSchedule schedule = schedules.stream()
                    .filter(s -> s.getScheduleId().equals(scheduleId))
                    .findFirst()
                    .orElse(null);

            if (schedule == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Schedule not found!");
                return "redirect:/admin/schedules";
            }

            // Check if seats already exist
            List<Seat> existingSeats = seatRepository.findByScheduleIdOrderByCoachAndSeat(scheduleId);
            if (!existingSeats.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Seats already exist for this schedule. Delete existing seats first to regenerate.");
                return "redirect:/admin/seats/schedule/" + scheduleId;
            }

            // Generate seats with A, B, C classes - 5 coaches each with 20 seats
            int totalGenerated = 0;
            
            // Generate seats for Class A (A1, A2, A3, A4, A5 coaches)
            for (int coachNum = 1; coachNum <= 5; coachNum++) {
                String coach = "A" + coachNum;
                for (int seat = 1; seat <= 20; seat++) {
                    Seat newSeat = new Seat();
                    newSeat.setSeatNumber(String.format("%s-W%d", coach, seat));
                    newSeat.setCoachNum(coach);
                    newSeat.setAvailable(true);
                    newSeat.setTrain(schedule.getTrain());
                    newSeat.setSchedule(schedule);
                    seatRepository.save(newSeat);
                    totalGenerated++;
                }
            }
            
            // Generate seats for Class B (B1, B2, B3, B4, B5 coaches)
            for (int coachNum = 1; coachNum <= 5; coachNum++) {
                String coach = "B" + coachNum;
                for (int seat = 1; seat <= 20; seat++) {
                    Seat newSeat = new Seat();
                    newSeat.setSeatNumber(String.format("%s-W%d", coach, seat));
                    newSeat.setCoachNum(coach);
                    newSeat.setAvailable(true);
                    newSeat.setTrain(schedule.getTrain());
                    newSeat.setSchedule(schedule);
                    seatRepository.save(newSeat);
                    totalGenerated++;
                }
            }
            
            // Generate seats for Class C (C1, C2, C3, C4, C5 coaches)
            for (int coachNum = 1; coachNum <= 5; coachNum++) {
                String coach = "C" + coachNum;
                for (int seat = 1; seat <= 20; seat++) {
                    Seat newSeat = new Seat();
                    newSeat.setSeatNumber(String.format("%s-W%d", coach, seat));
                    newSeat.setCoachNum(coach);
                    newSeat.setAvailable(true);
                    newSeat.setTrain(schedule.getTrain());
                    newSeat.setSchedule(schedule);
                    seatRepository.save(newSeat);
                    totalGenerated++;
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Successfully generated %d seats across 15 coaches (A1-A5, B1-B5, C1-C5) for schedule %s", 
                    totalGenerated, schedule.getScheduleId()));

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error generating seats: " + e.getMessage());
        }

        return "redirect:/admin/seats/schedule/" + scheduleId;
    }

    // Delete all seats for a schedule
    @PostMapping("/delete-all/{scheduleId}")
    public String deleteAllSeats(@PathVariable("scheduleId") Long scheduleId,
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
            List<Seat> seats = seatRepository.findByScheduleIdOrderByCoachAndSeat(scheduleId);
            int deletedCount = seats.size();
            
            for (Seat seat : seats) {
                seatRepository.delete(seat);
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Successfully deleted %d seats for this schedule", deletedCount));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting seats: " + e.getMessage());
        }

        return "redirect:/admin/seats/schedule/" + scheduleId;
    }

    // Toggle seat availability
    @PostMapping("/toggle/{seatId}")
    public String toggleSeatAvailability(@PathVariable("seatId") Long seatId,
                                        @RequestParam("scheduleId") Long scheduleId,
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
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null) {
                seat.setAvailable(!seat.isAvailable());
                seatRepository.save(seat);
                redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Seat %s is now %s", seat.getSeatNumber(), 
                        seat.isAvailable() ? "available" : "occupied"));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Seat not found!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating seat: " + e.getMessage());
        }

        return "redirect:/admin/seats/schedule/" + scheduleId;
    }

    // Populate seats for all schedules (admin utility)
    @PostMapping("/populate-all")
    @Transactional
    public String populateAllSeats(RedirectAttributes redirectAttributes) {
        try {
            // Get current user authentication
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = null;
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                username = (String) principal;
            }
            
            if (username == null) {
                return "redirect:/login";
            }

            // Get all schedules
            List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
            int totalGenerated = 0;
            int schedulesProcessed = 0;

            for (TrainSchedule schedule : schedules) {
                // Check if seats already exist for this schedule
                List<Seat> existingSeats = seatRepository.findByScheduleIdOrderByCoachAndSeat(schedule.getScheduleId());
                if (existingSeats.isEmpty()) {
                    // Generate seats for this schedule (15 coaches: A1-A5, B1-B5, C1-C5, 20 seats each)
                    for (String coachPrefix : new String[]{"A", "B", "C"}) {
                        for (int coach = 1; coach <= 5; coach++) { // 5 coaches per prefix
                            String coachNum = coachPrefix + coach;
                            for (int seat = 1; seat <= 20; seat++) {
                                Seat newSeat = new Seat();
                                newSeat.setSeatNumber(String.format("%s-W%d", coachNum, seat));
                                newSeat.setCoachNum(coachNum);
                                newSeat.setAvailable(true);
                                newSeat.setTrain(schedule.getTrain());
                                newSeat.setSchedule(schedule);
                                seatRepository.save(newSeat);
                                totalGenerated++;
                            }
                        }
                    }
                    schedulesProcessed++;
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Successfully generated %d seats for %d schedules", totalGenerated, schedulesProcessed));

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error populating seats: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }

    // Show seat generation form
    @GetMapping("/generate/{scheduleId}")
    public String showGenerateSeatsForm(@PathVariable("scheduleId") Long scheduleId, Model model) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }

        // Get schedule details
        List<TrainSchedule> schedules = trainScheduleService.getAllSchedules();
        TrainSchedule schedule = schedules.stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            return "redirect:/admin/schedules";
        }

        model.addAttribute("schedule", schedule);
        return "admin/generate-seats";
    }

    // Clean up orphaned seats (seats with null schedule_id)
    @PostMapping("/cleanup-orphaned")
    @Transactional
    public String cleanupOrphanedSeats(RedirectAttributes redirectAttributes) {
        try {
            // Check authentication and authorization
            if (!rbac.isAuthenticated()) {
                return "redirect:/login";
            }
            
            Passenger currentUser = rbac.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user has admin access or is train station master
            if (!rbac.hasAdminStaffAccess() && currentUser.getRole() != Passenger.UserRole.TRAIN_STATION_MASTER) {
                return "redirect:/access-denied";
            }

            // Find and delete orphaned seats
            List<Seat> orphanedSeats = seatRepository.findByScheduleIdIsNull();
            int deletedCount = orphanedSeats.size();
            
            if (deletedCount > 0) {
                for (Seat seat : orphanedSeats) {
                    seatRepository.delete(seat);
                }
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Successfully cleaned up %d orphaned seats (seats with null schedule_id). These seats were not associated with any schedule and have been removed from the database.", deletedCount));
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "No orphaned seats found. All seats are properly associated with schedules.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error cleaning up orphaned seats: " + e.getMessage());
        }

        return "redirect:/admin/schedules";
    }
}
