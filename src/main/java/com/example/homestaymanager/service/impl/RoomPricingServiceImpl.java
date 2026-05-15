package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateRoomPricingRequest;
import com.example.homestaymanager.model.RoomPricing;
import com.example.homestaymanager.repository.RoomPricingRepository;
import com.example.homestaymanager.service.RoomPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    @Override
    public List<RoomPricing> getListRoomPricing() {
        return roomPricingRepository.findAll();
    }

    @Override
    public RoomPricing updateRoomPricingById(int id, UpdateRoomPricingRequest request) {
        RoomPricing roomPricing = roomPricingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPricing not found"));

        if (request.getBaseDuration() != null && !request.getBaseDuration().isBlank()) {
            roomPricing.setBaseDuration(request.getBaseDuration());
        }

        if (request.getBasePrice() != null && request.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
            roomPricing.setBasePrice(request.getBasePrice());
        }

        if (request.getWeekendPrice() != null && request.getWeekendPrice().compareTo(BigDecimal.ZERO) > 0) {
            roomPricing.setWeekendPrice(request.getWeekendPrice());
        }

        if (request.getHolidayPrice() != null && request.getHolidayPrice().compareTo(BigDecimal.ZERO) > 0) {
            roomPricing.setHolidayPrice(request.getHolidayPrice());
        }

        if (request.getStartDate() != null) {
            roomPricing.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            roomPricing.setEndDate(request.getEndDate());
        }

        if (request.getPolicy() != null && !request.getPolicy().isBlank()) {
            roomPricing.setPolicy(request.getPolicy());
        }

        if (request.getStatus() != null) {
            roomPricing.setStatus(request.getStatus());
        }

        return roomPricingRepository.save(roomPricing);
    }
}