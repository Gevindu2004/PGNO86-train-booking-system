package com.trainmanagement.trainmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "train")
@Data
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String route;  // e.g., "Colombo-Kandy"

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<TrainSchedule> schedules;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL)
    private List<Seat> seats;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    
    public List<TrainSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<TrainSchedule> schedules) { this.schedules = schedules; }
    
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}