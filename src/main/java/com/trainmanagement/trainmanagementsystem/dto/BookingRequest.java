package com.trainmanagement.trainmanagementsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long scheduleId;
    private String passengerName;
    private List<Long> seatIds;  // Seats to book
}