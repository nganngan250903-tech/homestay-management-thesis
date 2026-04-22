package com.example.homestaymanager.repository;

import com.example.homestaymanager.model.RoomPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomPricingRepository extends JpaRepository<RoomPricing, Integer> {
}