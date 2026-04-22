package com.example.homestaymanager.service;

import com.example.homestaymanager.model.RoomPricing;

public interface RoomPricingService {

    Integer createRoomPricing(RoomPricing roomPricing);

    RoomPricing getRoomPricingByID(int id);

    void deleteRoomPricingById(int id);
}