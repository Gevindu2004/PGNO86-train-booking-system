package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public void createAlert(Alert alert, Passenger recipient) {
        // Ensure the alert is properly associated with the recipient
        alert.setPostedBy(recipient);
        alert.setPostedAt(LocalDateTime.now());
        System.out.println("[DEBUG] AlertService - Creating alert for user: " + recipient.getUsername());
        System.out.println("[DEBUG] AlertService - Alert message: " + alert.getMessage().substring(0, Math.min(50, alert.getMessage().length())) + "...");
        alertRepository.save(alert);
        System.out.println("[DEBUG] AlertService - Alert saved successfully");
    }

    public List<Alert> findAllSorted() {
        return alertRepository.findAllByOrderByPostedAtDesc();
    }
    
    public List<Alert> findByUserSorted(Passenger passenger) {
        System.out.println("[DEBUG] AlertService - Finding alerts for user: " + passenger.getUsername());
        List<Alert> alerts = alertRepository.findByPostedByOrderByPostedAtDesc(passenger);
        System.out.println("[DEBUG] AlertService - Found " + alerts.size() + " alerts for user: " + passenger.getUsername());
        return alerts;
    }
    
    public List<Alert> findGeneralAlertsSorted() {
        System.out.println("[DEBUG] AlertService - Finding general alerts (posted by admin users)");
        List<Alert> alerts = alertRepository.findAllByOrderByPostedAtDesc();
        
        // Filter to only include alerts posted by admin users (non-passenger roles)
        List<Alert> generalAlerts = alerts.stream()
                .filter(alert -> alert.getPostedBy() != null && 
                        alert.getPostedBy().getRole() != Passenger.UserRole.PASSENGER)
                .collect(java.util.stream.Collectors.toList());
        
        System.out.println("[DEBUG] AlertService - Found " + generalAlerts.size() + " general alerts");
        return generalAlerts;
    }
    
    public List<Alert> findSystemCriticalAlertsSorted() {
        System.out.println("[DEBUG] AlertService - Finding system critical alerts (delays, maintenance, cancellations)");
        List<Alert> alerts = alertRepository.findAllByOrderByPostedAtDesc();
        
        // Filter to include:
        // 1. Alerts posted by admin users (non-passenger roles)
        // 2. System-critical alerts (delays, maintenance, cancellations) regardless of who posted them
        List<Alert> systemAlerts = alerts.stream()
                .filter(alert -> alert.getPostedBy() != null && (
                    // Admin-posted alerts
                    alert.getPostedBy().getRole() != Passenger.UserRole.PASSENGER ||
                    // System-critical alerts (delays, maintenance, cancellations)
                    (alert.getMessage() != null && (
                        alert.getMessage().toUpperCase().contains("DELAY") ||
                        alert.getMessage().toUpperCase().contains("MAINTENANCE") ||
                        alert.getMessage().toUpperCase().contains("CANCELLATION") ||
                        alert.getMessage().toUpperCase().contains("CANCELLED")
                    ))
                ))
                .collect(java.util.stream.Collectors.toList());
        
        System.out.println("[DEBUG] AlertService - Found " + systemAlerts.size() + " system critical alerts");
        return systemAlerts;
    }
    
    public List<Alert> findAlertsForGuest() {
        System.out.println("[DEBUG] AlertService - Finding alerts for guest user");
        
        // For guest users, show system-critical alerts (delays, maintenance, cancellations)
        List<Alert> systemAlerts = findSystemCriticalAlertsSorted();
        
        System.out.println("[DEBUG] AlertService - Found " + systemAlerts.size() + " alerts for guest user");
        return systemAlerts;
    }
    
    public List<Alert> findAlertsForUser(Passenger user) {
        System.out.println("[DEBUG] AlertService - Finding alerts for user: " + user.getUsername());
        
        // Get system-critical alerts (delays, maintenance, cancellations from anyone)
        List<Alert> systemAlerts = findSystemCriticalAlertsSorted();
        
        // Get personal alerts (posted by this specific user)
        List<Alert> personalAlerts = findByUserSorted(user);
        
        // Combine both lists
        List<Alert> allAlerts = new java.util.ArrayList<>();
        allAlerts.addAll(systemAlerts);
        allAlerts.addAll(personalAlerts);
        
        // Sort by posted date (most recent first)
        allAlerts.sort((a1, a2) -> a2.getPostedAt().compareTo(a1.getPostedAt()));
        
        System.out.println("[DEBUG] AlertService - Found " + systemAlerts.size() + " system-critical + " + personalAlerts.size() + " personal = " + allAlerts.size() + " total alerts for user: " + user.getUsername());
        return allAlerts;
    }

    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new RuntimeException("Alert not found with id: " + id);
        }
        alertRepository.deleteById(id);
    }
}
