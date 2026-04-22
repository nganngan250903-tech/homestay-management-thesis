package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.RoomPricing;
import com.example.homestaymanager.repository.RoomPricingRepository;
import com.example.homestaymanager.service.RoomPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RoomPricingServiceImpl implements RoomPricingService {

    private final RoomPricingRepository roomPricingRepository;

    @Override
    public Integer createRoomPricing(RoomPricing roomPricing) {

        if (roomPricing.getBaseDuration() == null || roomPricing.getBaseDuration().trim().isEmpty()) {
            throw new RuntimeException("baseDuration không được để trống");
        }

        if (roomPricing.getBasePrice() == null || roomPricing.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("basePrice phải > 0");
        }

        if (roomPricing.getWeekendPrice() == null || roomPricing.getWeekendPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("weekendPrice phải > 0");
        }

        if (roomPricing.getHolidayPrice() == null || roomPricing.getHolidayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("holidayPrice phải > 0");
        }

        roomPricingRepository.save(roomPricing);
        return roomPricing.getId();
    }

    @Override
    public RoomPricing getRoomPricingByID(int id) {
        return roomPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPricing not found"));
    }

    @Override
    public void deleteRoomPricingById(int id) {
        RoomPricing roomPricing = roomPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPricing not found"));

        roomPricingRepository.delete(roomPricing);
    }
}