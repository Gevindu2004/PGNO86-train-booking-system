package com.trainmanagement.trainmanagementsystem.repository;

import com.trainmanagement.trainmanagementsystem.entity.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    
    /**
     * Find pricing by route and class type
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return Optional containing the pricing if found
     */
    @Query("SELECT p FROM Pricing p WHERE p.fromStation = :fromStation AND p.toStation = :toStation AND p.classType = :classType")
    Optional<Pricing> findByRouteAndClass(@Param("fromStation") String fromStation, 
                                         @Param("toStation") String toStation, 
                                         @Param("classType") Pricing.ClassType classType);
    
    /**
     * Get price for a specific route and class
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return price as BigDecimal, or null if not found
     */
    @Query("SELECT p.price FROM Pricing p WHERE p.fromStation = :fromStation AND p.toStation = :toStation AND p.classType = :classType")
    Optional<BigDecimal> findPriceByRouteAndClass(@Param("fromStation") String fromStation, 
                                                 @Param("toStation") String toStation, 
                                                 @Param("classType") Pricing.ClassType classType);
    
    /**
     * Check if pricing exists for a route and class
     * @param fromStation departure station
     * @param toStation arrival station
     * @param classType class type (A, B, or C)
     * @return true if pricing exists, false otherwise
     */
    @Query("SELECT COUNT(p) > 0 FROM Pricing p WHERE p.fromStation = :fromStation AND p.toStation = :toStation AND p.classType = :classType")
    boolean existsByRouteAndClass(@Param("fromStation") String fromStation, 
                                 @Param("toStation") String toStation, 
                                 @Param("classType") Pricing.ClassType classType);
}
