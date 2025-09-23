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
    private String status = "ON_TIME"; // Default status: ON_TIME, DELAYED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY) // Add fetch type
    @JoinColumn(name = "train_id")
    private Train train; // Make sure Train entity exists and is imported

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }
    
    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
}

