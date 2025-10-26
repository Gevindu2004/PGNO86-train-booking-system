package com.trainmanagement.trainmanagementsystem.config;

import com.trainmanagement.trainmanagementsystem.entity.Train;
import com.trainmanagement.trainmanagementsystem.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TrainRepository trainRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if trains already exist
        if (trainRepository.count() == 0) {
            // Create sample trains
            Train train1 = new Train();
            train1.setName("Express Train 1");
            train1.setRoute("Colombo - Kandy");
            trainRepository.save(train1);

            Train train2 = new Train();
            train2.setName("Express Train 2");
            train2.setRoute("Kandy - Ella");
            trainRepository.save(train2);

            Train train3 = new Train();
            train3.setName("Local Train 1");
            train3.setRoute("Polgahawela - Colombo");
            trainRepository.save(train3);

            Train train4 = new Train();
            train4.setName("Intercity Train 1");
            train4.setRoute("Colombo - Galle");
            trainRepository.save(train4);

            Train train5 = new Train();
            train5.setName("Intercity Train 2");
            train5.setRoute("Colombo - Anuradhapura");
            trainRepository.save(train5);

            System.out.println("Sample trains created successfully!");
        }
    }
}
