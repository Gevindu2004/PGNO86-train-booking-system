package com.trainmanagement.trainmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String fromStation;
    private String toStation;

    @ManyToOne(fetch = FetchType.LAZY) // Add fetch type
    @JoinColumn(name = "train_id")
    private Train train; // Make sure Train entity exists and is imported
}

