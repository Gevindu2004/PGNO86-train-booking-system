package com.trainmanagement.trainmanagementsystem;

import com.trainmanagement.trainmanagementsystem.entity.*;
import com.trainmanagement.trainmanagementsystem.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootApplication
public class TrainManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainManagementSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(TrainRepository trainRepo, ScheduleRepository scheduleRepo, SeatRepository seatRepo) {
        return args -> {
            Train train = new Train();
            train.setName("Express 101");
            train.setRoute("Colombo-Kandy");
            train = trainRepo.save(train);

            Schedule schedule = new Schedule();
            schedule.setDate(LocalDate.now().plusDays(1));
            schedule.setDepartureTime(LocalTime.of(8, 0));
            schedule.setArrivalTime(LocalTime.of(11, 0));
            schedule.setFromStation("Colombo");
            schedule.setToStation("Kandy");
            schedule.setTrain(train);
            schedule = scheduleRepo.save(schedule);

            Seat seat1 = new Seat();
            seat1.setSeatNumber("A1");
            seat1.setTrain(train);
            seat1.setSchedule(schedule);
            seatRepo.save(seat1);


        };
    }
}
