package com.trainmanagement.trainmanagementsystem.service;

import com.trainmanagement.trainmanagementsystem.entity.Pricing;
import com.trainmanagement.trainmanagementsystem.repository.PricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PricingService {
    
    @Autowired
    private PricingRepository pricingRepository;
    
    /**
     * Get price for a specific route and class type
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return price as double, or 0.0 if not found
     */
    public double getPrice(String fromStation, String toStation, String classType) {
        try {
            Pricing.ClassType enumClassType = Pricing.ClassType.valueOf(classType.toUpperCase());
            Optional<BigDecimal> price = pricingRepository.findPriceByRouteAndClass(fromStation, toStation, enumClassType);
            return price.map(BigDecimal::doubleValue).orElse(0.0);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid class type: " + classType);
            return 0.0;
        }
    }
    
    /**
     * Get price for a specific route and class type with BigDecimal return
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return Optional containing the price if found
     */
    public Optional<BigDecimal> getPriceAsBigDecimal(String fromStation, String toStation, String classType) {
        try {
            Pricing.ClassType enumClassType = Pricing.ClassType.valueOf(classType.toUpperCase());
            return pricingRepository.findPriceByRouteAndClass(fromStation, toStation, enumClassType);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid class type: " + classType);
            return Optional.empty();
        }
    }
    
    /**
     * Get full pricing information for a route and class
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return Optional containing the pricing entity if found
     */
    public Optional<Pricing> getPricingInfo(String fromStation, String toStation, String classType) {
        try {
            Pricing.ClassType enumClassType = Pricing.ClassType.valueOf(classType.toUpperCase());
            return pricingRepository.findByRouteAndClass(fromStation, toStation, enumClassType);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid class type: " + classType);
            return Optional.empty();
        }
    }
    
    /**
     * Check if pricing exists for a route and class
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return true if pricing exists, false otherwise
     */
    public boolean pricingExists(String fromStation, String toStation, String classType) {
        try {
            Pricing.ClassType enumClassType = Pricing.ClassType.valueOf(classType.toUpperCase());
            return pricingRepository.existsByRouteAndClass(fromStation, toStation, enumClassType);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid class type: " + classType);
            return false;
        }
    }
    
    /**
     * Get class name from class type
     * @param classType class type (A, B, or C)
     * @return formatted class name
     */
    public String getClassName(String classType) {
        switch (classType.toUpperCase()) {
            case "A":
                return "First Class";
            case "B":
                return "Second Class";
            case "C":
                return "Third Class";
            default:
                return "Class " + classType;
        }
    }
    
    /**
     * Get all pricing records
     * @return list of all pricing records
     */
    public List<Pricing> getAllPricing() {
        return pricingRepository.findAll();
    }
    
    /**
     * Save or update a pricing record
     * @param pricing the pricing entity to save
     * @return the saved pricing entity
     */
    public Pricing savePricing(Pricing pricing) {
        return pricingRepository.save(pricing);
    }
    
    /**
     * Delete a pricing record by ID
     * @param id the ID of the pricing record to delete
     */
    public void deletePricing(Long id) {
        pricingRepository.deleteById(id);
    }
    
    /**
     * Get pricing by ID
     * @param id the ID of the pricing record
     * @return Optional containing the pricing if found
     */
    public Optional<Pricing> getPricingById(Long id) {
        return pricingRepository.findById(id);
    }
    
    /**
     * Create or update pricing for a route and class
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @param price the price to set
     * @return the saved pricing entity
     */
    public Pricing createOrUpdatePricing(String fromStation, String toStation, String classType, BigDecimal price) {
        try {
            Pricing.ClassType enumClassType = Pricing.ClassType.valueOf(classType.toUpperCase());
            
            // Check if pricing already exists
            Optional<Pricing> existingPricing = pricingRepository.findByRouteAndClass(fromStation, toStation, enumClassType);
            
            if (existingPricing.isPresent()) {
                // Update existing pricing
                Pricing pricing = existingPricing.get();
                pricing.setPrice(price);
                return pricingRepository.save(pricing);
            } else {
                // Create new pricing
                Pricing newPricing = new Pricing(fromStation, toStation, enumClassType, price);
                return pricingRepository.save(newPricing);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid class type: " + classType);
        }
    }
    
    /**
     * Get all unique routes (from_station, to_station combinations)
     * @return list of route strings in format "fromStation - toStation"
     */
    public List<String> getAllRoutes() {
        return pricingRepository.findAll().stream()
                .map(p -> p.getFromStation() + " - " + p.getToStation())
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Get all unique stations
     * @return list of all unique station names
     */
    public List<String> getAllStations() {
        return pricingRepository.findAll().stream()
                .flatMap(p -> java.util.stream.Stream.of(p.getFromStation(), p.getToStation()))
                .distinct()
                .sorted()
                .toList();
    }
}
