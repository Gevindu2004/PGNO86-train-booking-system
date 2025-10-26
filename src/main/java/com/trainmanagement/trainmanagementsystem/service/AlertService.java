package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public void createAlert(Alert alert, Passenger admin) {
        alert.setPostedBy(admin);
        alert.setPostedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    public List<Alert> findAllSorted() {
        return alertRepository.findAllByOrderByPostedAtDesc();
    }

    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new RuntimeException("Alert not found with id: " + id);
        }
        alertRepository.deleteById(id);
    }
}
