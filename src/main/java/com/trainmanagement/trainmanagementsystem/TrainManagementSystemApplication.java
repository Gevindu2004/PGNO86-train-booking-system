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

    // Removed CommandLineRunner - using data.sql for initialization instead
}
