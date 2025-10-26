package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.AlertService;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AlertController {

    @Autowired
    private AlertService alertService;
    
    @Autowired
    private PassengerService passengerService;

    // Public endpoint to view alerts - shows general alerts to all, personal alerts to logged-in users
    @GetMapping("/alerts")
    public String viewAllAlerts(Model model) {
        try {
            // Check if user is logged in
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = null;
            if (principal instanceof String && !"anonymousUser".equals(principal)) {
                username = (String) principal;
            }
            
            List<Alert> alertList;
            
            if (username != null) {
                // User is logged in - show general alerts + their personal alerts
                Passenger currentUser = passengerService.findByUsername(username).orElse(null);
                if (currentUser != null) {
                    alertList = alertService.findAlertsForUser(currentUser);
                    model.addAttribute("currentUser", currentUser);
                    System.out.println("[DEBUG] AlertController - Showing personalized alerts for logged-in user: " + username);
                } else {
                    // Fallback to general alerts only
                    alertList = alertService.findGeneralAlertsSorted();
                    System.out.println("[DEBUG] AlertController - User not found, showing general alerts only");
                }
            } else {
                // Guest user - show system-critical alerts (delays, maintenance, cancellations)
                alertList = alertService.findAlertsForGuest();
                System.out.println("[DEBUG] AlertController - Showing system-critical alerts for guest user");
            }
            
            model.addAttribute("alerts", alertList);
            model.addAttribute("isAuthenticated", username != null);
            
        } catch (Exception e) {
            System.err.println("[ERROR] AlertController - Error loading alerts: " + e.getMessage());
            e.printStackTrace();
            // Fallback to empty list
            model.addAttribute("alerts", new java.util.ArrayList<Alert>());
            model.addAttribute("isAuthenticated", false);
        }
        
        return "alerts";
    }

}
