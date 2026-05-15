package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateRoomRequest;
import com.example.homestaymanager.model.Room;
import java.util.List;

public interface RoomService {
    Integer createRoom(Room room);

    Room getRoomByID(int id);

    void deleteRoomById(int id);

    List<Room> getListRoom();

    Room updateRoomById(int id, UpdateRoomRequest request);
}