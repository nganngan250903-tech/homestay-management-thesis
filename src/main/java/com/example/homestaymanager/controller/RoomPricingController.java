package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.RoomPricing;
import com.example.homestaymanager.service.RoomPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomPricingController {

    private final RoomPricingService roomPricingService;

    @PostMapping("/roomPricings")
    public Integer createRoomPricing(@RequestBody RoomPricing roomPricing) {
        return roomPricingService.createRoomPricing(roomPricing);
    }

    @GetMapping("/roomPricings/{roomPricingId}")
    public RoomPricing getRoomPricingById(@PathVariable int roomPricingId) {
        return roomPricingService.getRoomPricingByID(roomPricingId);
    }

    @DeleteMapping("/roomPricings/{roomPricingId}")
    public void deleteRoomPricingById(@PathVariable int roomPricingId) {
        roomPricingService.deleteRoomPricingById(roomPricingId);
    }
}