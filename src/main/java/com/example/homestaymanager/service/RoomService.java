package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Room;

public interface RoomService {
    Integer createRoom(Room room);

    Room getRoomByID(int id);

    void deleteRoomById(int id);
}