package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.PassengerFeedback;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.service.PassengerFeedbackService;
import com.trainmanagement.trainmanagementsystem.service.PassengerService;
import com.trainmanagement.trainmanagementsystem.util.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private PassengerFeedbackService feedbackService;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private RoleBasedAccessControl rbac;

    @GetMapping("/new")
    public String showFeedbackForm(Model model, HttpSession session) {
        model.addAttribute("feedback", new PassengerFeedback());
        
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        model.addAttribute("isLoggedIn", username != null);
        return "feedback-form";
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") PassengerFeedback feedback, 
                                HttpSession session) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login?redirect=/feedback/my-feedback";
        }
        
        Passenger passenger = passengerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        feedbackService.saveFeedback(feedback, passenger);
        return "redirect:/feedback/my-feedback";
    }

    @GetMapping("/my-feedback")
    public String viewMyFeedback(Model model, HttpSession session) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            // If not logged in, show empty feedback list with login prompt
            model.addAttribute("feedbackList", new java.util.ArrayList<>());
            model.addAttribute("notLoggedIn", true);
            return "my-feedback";
        }
        
        Passenger passenger = passengerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<PassengerFeedback> feedbackList = feedbackService.findFeedbackByPassenger(passenger);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("notLoggedIn", false);
        return "my-feedback";
    }

    // Admin/Analyst endpoints
    @GetMapping("/manage")
    public String manageFeedback(Model model, HttpSession session) {
        // Check authentication and feedback access
        if (!rbac.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // Only passenger experience analysts and admin staff can access feedback management
        if (!rbac.canAccessFeedbackManagement()) {
            return "redirect:/access-denied";
        }
        
        List<PassengerFeedback> feedbackList = feedbackService.findAll();
        model.addAttribute("feedbackList", feedbackList);
        return "feedback-list";
    }

    @PostMapping("/update/{id}")
    public String updateFeedback(@PathVariable Long id, 
                                @RequestParam String status,
                                @RequestParam(required = false) String response,
                                HttpSession session) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        Passenger analyst = passengerService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        feedbackService.updateStatus(id, status, response, analyst);
        return "redirect:/feedback/manage";
    }

    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id, HttpSession session) {
        // Get current user authentication
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof String && !"anonymousUser".equals(principal)) {
            username = (String) principal;
        }
        
        if (username == null) {
            return "redirect:/login";
        }
        
        feedbackService.deleteFeedback(id);
        return "redirect:/feedback/manage";
    }
}
