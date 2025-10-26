package com.trainmanagement.trainmanagementsystem.util;

import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedAccessControl {

    @Autowired
    private PassengerService passengerService;

    /**
     * Get current authenticated user
     */
    public Passenger getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return null;
        }
        
        return passengerService.findByUsername(username).orElse(null);
    }

    /**
     * Check if current user has admin staff role
     */
    public boolean hasAdminStaffAccess() {
        Passenger user = getCurrentUser();
        return user != null && user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if current user can access admin dashboard
     */
    public boolean canAccessAdminDashboard() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        return user.getRole() == Passenger.UserRole.ADMIN_STAFF ||
               user.getRole() == Passenger.UserRole.TRAIN_STATION_MASTER;
    }

    /**
     * Check if current user can access feedback management
     */
    public boolean canAccessFeedbackManagement() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        return user.getRole() == Passenger.UserRole.PASSENGER_EXPERIENCE_ANALYST ||
               user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if current user can access train schedule management
     */
    public boolean canAccessScheduleManagement() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        return user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if current user can access alert management
     */
    public boolean canAccessAlertManagement() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        return user.getRole() == Passenger.UserRole.TRAIN_STATION_MASTER ||
               user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if current user can access passenger booking functionality
     */
    public boolean canAccessPassengerBooking() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        // All roles except specific restrictions can access booking
        return user.getRole() == Passenger.UserRole.PASSENGER ||
               user.getRole() == Passenger.UserRole.TICKET_OFFICER ||
               user.getRole() == Passenger.UserRole.PASSENGER_EXPERIENCE_ANALYST ||
               user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if current user can access pricing management
     */
    public boolean canAccessPricingManagement() {
        Passenger user = getCurrentUser();
        if (user == null) return false;
        
        return user.getRole() == Passenger.UserRole.TICKET_OFFICER ||
               user.getRole() == Passenger.UserRole.ADMIN_STAFF;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Get user's role name for display
     */
    public String getUserRoleDisplayName(Passenger.UserRole role) {
        switch (role) {
            case TRAIN_STATION_MASTER:
                return "Train Station Master";
            case TICKET_OFFICER:
                return "Ticket Officer";
            case PASSENGER_EXPERIENCE_ANALYST:
                return "Passenger Experience Analyst";
            case ADMIN_STAFF:
                return "Admin Staff";
            case PASSENGER:
            default:
                return "Passenger";
        }
    }
}
