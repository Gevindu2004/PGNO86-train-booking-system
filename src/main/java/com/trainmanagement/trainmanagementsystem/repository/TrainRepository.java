package com.trainmanagement.trainmanagementsystem.repository;


import com.trainmanagement.trainmanagementsystem.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}
