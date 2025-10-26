package com.trainmanagement.trainmanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class TrainSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "from_station")
    private String fromStation;

    @Column(name = "to_station")
    private String toStation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ScheduleStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "train_id")
    private Train train;

    // Additional fields for admin management
    @Transient
    private String scheduleID; // For display purposes

    @Transient
    private String trainNumber; // For display purposes

    @Transient
    private String route; // For display purposes

    @Transient
    private String platform; // For display purposes

    @Transient
    private LocalDateTime lastUpdated; // For display purposes

    public enum ScheduleStatus {
        ON_TIME, DELAYED, CANCELLED
    }

    // Constructors
    public TrainSchedule() {}

    // Getters and Setters
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

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

    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    
    // Helper method for compatibility
    public Long getTrainId() { 
        return train != null ? train.getId() : null; 
    }
    public void setTrainId(Long trainId) { 
        if (train == null) {
            train = new Train();
        }
        train.setId(trainId); 
    }

    // Transient field getters and setters for compatibility
    public String getScheduleID() { 
        return scheduleID != null ? scheduleID : "S" + String.format("%06d", scheduleId);
    }
    public void setScheduleID(String scheduleID) { this.scheduleID = scheduleID; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getRoute() { 
        return route != null ? route : (fromStation + " - " + toStation);
    }
    public void setRoute(String route) { this.route = route; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getLastUpdated() { 
        return lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    // Helper methods for status conversion
    public String getStatusString() {
        if (status == null) return "OnTime";
        switch (status) {
            case ON_TIME: return "OnTime";
            case DELAYED: return "Delayed";
            case CANCELLED: return "Cancelled";
            default: return "OnTime";
        }
    }

    public void setStatusString(String statusString) {
        switch (statusString) {
            case "OnTime": this.status = ScheduleStatus.ON_TIME; break;
            case "Delayed": this.status = ScheduleStatus.DELAYED; break;
            case "Cancelled": this.status = ScheduleStatus.CANCELLED; break;
            default: this.status = ScheduleStatus.ON_TIME;
        }
    }
}
