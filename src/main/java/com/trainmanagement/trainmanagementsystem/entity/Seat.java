package com.trainmanagement.trainmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "seat")
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seat_number")
    private String seatNumber;  // e.g., "W1"
    
    @Column(name = "coach_num")
    private String coachNum;    // e.g., "A1"
    
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;  // Tie availability to specific schedule/date

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    
    public String getCoachNum() { return coachNum; }
    public void setCoachNum(String coachNum) { this.coachNum = coachNum; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
}