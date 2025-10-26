package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.PassengerFeedback;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.repository.PassengerFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PassengerFeedbackService {

    @Autowired
    private PassengerFeedbackRepository feedbackRepository;

    public void saveFeedback(PassengerFeedback feedback, Passenger passenger) {
        feedback.setPassenger(passenger);
        feedback.setStatus("New");
        feedback.setSubmittedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }

    public List<PassengerFeedback> findFeedbackByPassenger(Passenger passenger) {
        return feedbackRepository.findByPassenger(passenger);
    }

    public List<PassengerFeedback> findAll() {
        return feedbackRepository.findAll();
    }

    public void updateStatus(Long feedbackId, String status, String response, Passenger analyst) {
        PassengerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid feedback Id:" + feedbackId));
        feedback.setStatus(status);
        feedback.setResponse(response);
        feedback.setUpdatedBy(analyst);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long feedbackId) {
        PassengerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid feedback Id:" + feedbackId));

        if (!"Resolved".equals(feedback.getStatus())) {
            throw new IllegalStateException("Feedback must be in 'Resolved' status to be deleted.");
        }

        feedbackRepository.deleteById(feedbackId);
    }
}
