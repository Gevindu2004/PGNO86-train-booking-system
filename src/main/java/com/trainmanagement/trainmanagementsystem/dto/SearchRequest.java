package com.trainmanagement.trainmanagementsystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SearchRequest {
    private String fromStation;
    private String toStation;
    private LocalDate date;
    private LocalTime time;  // Approximate departure time

    // Getters and Setters
    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }
    
    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
}