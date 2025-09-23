// BookingRequest.java
package com.trainmanagement.trainmanagementsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long scheduleId;
    private String passengerName;
    private List<Long> seatIds;  // Seats to book

    // Getters and Setters
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
}