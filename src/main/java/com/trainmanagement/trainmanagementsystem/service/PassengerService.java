package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.Passenger;
import com.trainmanagement.trainmanagementsystem.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Passenger> findByUsername(String username) {
        return passengerRepository.findByUsername(username);
    }

    public Optional<Passenger> findByEmail(String email) {
        return passengerRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return passengerRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return passengerRepository.existsByEmail(email);
    }

    public Passenger save(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public boolean authenticate(String usernameOrEmail, String password) {
        Optional<Passenger> passenger = findByUsername(usernameOrEmail);
        if (!passenger.isPresent()) {
            passenger = findByEmail(usernameOrEmail);
        }
        if (passenger.isPresent()) {
            return passwordEncoder.matches(password, passenger.get().getPassword());
        }
        return false;
    }

    public Passenger registerPassenger(String fullName, String email, String phone, String username, String password) {
        System.out.println("Attempting to register passenger: " + username);

        if (existsByUsername(username)) {
            System.out.println("Username already exists: " + username);
            throw new RuntimeException("Username already exists");
        }

        if (existsByEmail(email)) {
            System.out.println("Email already exists: " + email);
            throw new RuntimeException("Email already exists");
        }

        Passenger passenger = new Passenger();
        passenger.setFullName(fullName);
        passenger.setEmail(email);
        passenger.setPhone(phone);
        passenger.setUsername(username);
        passenger.setPassword(passwordEncoder.encode(password));

        System.out.println("Saving passenger to database...");
        Passenger savedPassenger = save(passenger);
        System.out.println("Passenger saved successfully with ID: " + savedPassenger.getId());

        return savedPassenger;
    }

    public long countPassengers() {
        return passengerRepository.count();
    }

    public Passenger updatePassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }
}
