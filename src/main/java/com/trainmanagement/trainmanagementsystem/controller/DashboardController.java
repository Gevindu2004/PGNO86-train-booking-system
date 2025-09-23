package com.trainmanagement.trainmanagementsystem.controller;
import com.trainmanagement.trainmanagementsystem.repository.BookingRepository;
import com.trainmanagement.trainmanagementsystem.repository.ScheduleRepository;
import com.trainmanagement.trainmanagementsystem.repository.SeatRepository;
import com.trainmanagement.trainmanagementsystem.repository.TrainRepository;
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
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public String dashboard(Model model) {
        long trains = trainRepository.count();
        long schedules = scheduleRepository.count();
        long seats = seatRepository.count();
        long bookings = bookingRepository.count();

        model.addAttribute("trainsCount", trains);
        model.addAttribute("schedulesCount", schedules);
        model.addAttribute("seatsCount", seats);
        model.addAttribute("bookingsCount", bookings);
        return "dashboard";
    }
}


