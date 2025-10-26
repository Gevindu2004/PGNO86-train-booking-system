package com.trainbooking.controller;

import com.trainbooking.model.Schedule;
import com.trainbooking.model.Train;
import com.trainbooking.service.ScheduleService;
import com.trainbooking.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/schedules")
public class AdminController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TrainService trainService;

    @GetMapping
    public String listSchedules(Model model) {
        List<Schedule> scheduleList = scheduleService.findAllSchedules();
        model.addAttribute("schedules", scheduleList);
        return "admin/schedules";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Schedule schedule = new Schedule();
        List<Train> trainList = trainService.findAll();
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trainList);
        return "admin/schedule-form";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Schedule schedule = scheduleService.findScheduleById(id);
        List<Train> trainList = trainService.findAll();
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trainList);
        return "admin/schedule-form";
    }

    @PostMapping("/save")
    public String saveSchedule(@ModelAttribute("schedule") Schedule schedule) {
        scheduleService.saveSchedule(schedule);
        return "redirect:/admin/schedules";
    }

    @PostMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/admin/schedules";
    }
}
