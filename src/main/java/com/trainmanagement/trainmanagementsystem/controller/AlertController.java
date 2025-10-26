package com.trainmanagement.trainmanagementsystem.controller;

import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AlertController {

    @Autowired
    private AlertService alertService;

    // Public endpoint to view all alerts
    @GetMapping("/alerts")
    public String viewAllAlerts(Model model) {
        List<Alert> alertList = alertService.findAllSorted();
        model.addAttribute("alerts", alertList);
        return "alerts";
    }

}
