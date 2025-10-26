package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.PassengerFeedback;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerFeedbackRepository extends JpaRepository<PassengerFeedback, Long> {
    List<PassengerFeedback> findByPassenger(Passenger passenger);
}
