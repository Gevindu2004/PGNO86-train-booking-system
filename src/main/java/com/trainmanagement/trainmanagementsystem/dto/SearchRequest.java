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
}