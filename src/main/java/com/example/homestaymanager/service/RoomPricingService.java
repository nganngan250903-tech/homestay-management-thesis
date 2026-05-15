package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateRoomPricingRequest;
import com.example.homestaymanager.model.RoomPricing;
import java.util.List;

public interface RoomPricingService {

    Integer createRoomPricing(RoomPricing roomPricing);

    RoomPricing getRoomPricingByID(int id);

    void deleteRoomPricingById(int id);

    List<RoomPricing> getListRoomPricing();

    RoomPricing updateRoomPricingById(int id, UpdateRoomPricingRequest request);
}