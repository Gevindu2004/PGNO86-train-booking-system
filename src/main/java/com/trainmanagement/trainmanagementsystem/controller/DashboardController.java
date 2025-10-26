package com.trainmanagement.trainmanagementsystem.controller;
import com.trainmanagement.trainmanagementsystem.repository.BookingRepository;
import com.trainmanagement.trainmanagementsystem.repository.TrainScheduleRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import com.trainmanagement.trainmanagementsystem.repository.TrainRepository;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrainScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoleBasedAccessControl rbac;

    @GetMapping
    public String dashboard(Model model) {
        // Check authentication
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Passenger currentUser = rbac.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Redirect to appropriate dashboard based on role
        switch (currentUser.getRole()) {
            case ADMIN_STAFF:
                return "redirect:/admin/dashboard";
            case TRAIN_STATION_MASTER:
                return "redirect:/admin/dashboard";
            case PASSENGER_EXPERIENCE_ANALYST:
                return "redirect:/admin/analyst/dashboard";
            case TICKET_OFFICER:
                return "redirect:/ticket-officer/dashboard";
            case PASSENGER:
            default:
                // For regular users, show the main dashboard
                long trains = trainRepository.count();
                long schedules = scheduleRepository.count();
                long seats = seatRepository.count();
                long bookings = bookingRepository.count();

                model.addAttribute("trainsCount", trains);
                model.addAttribute("schedulesCount", schedules);
                model.addAttribute("seatsCount", seats);
                model.addAttribute("bookingsCount", bookings);
                
                // Add user role information
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("userRole", currentUser.getRole());
                model.addAttribute("userRoleDisplay", rbac.getUserRoleDisplayName(currentUser.getRole()));
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("username", currentUser.getUsername());
                
                return "dashboard";
        }
    }
}


