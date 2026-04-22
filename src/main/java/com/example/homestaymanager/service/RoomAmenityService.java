package com.example.homestaymanager.service;

import com.example.homestaymanager.model.RoomAmenity;

public interface RoomAmenityService {

    Integer createRoomAmenity(RoomAmenity roomAmenity);

    RoomAmenity getRoomAmenityByID(int id);

    void deleteRoomAmenityById(int id);
}