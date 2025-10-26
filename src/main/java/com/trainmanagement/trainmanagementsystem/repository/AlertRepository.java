package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Alert;
import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByOrderByPostedAtDesc();
    
    @Query("SELECT a FROM Alert a WHERE a.postedBy = :passenger ORDER BY a.postedAt DESC")
    List<Alert> findByPostedByOrderByPostedAtDesc(@Param("passenger") Passenger passenger);
}
