package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.CreateRoomRequest;
import com.example.homestaymanager.dto.request.UpdateRoomRequest;
import com.example.homestaymanager.dto.response.RoomResponse;
import com.example.homestaymanager.enums.RoomStatus;
import java.util.List;

public interface RoomService {
    Integer createRoom(CreateRoomRequest request);

    RoomResponse getRoomByID(int id);

    void deleteRoomById(int id);

    List<RoomResponse> getListRoom();

    RoomResponse updateRoomById(int id, UpdateRoomRequest request);

    RoomResponse updateRoomStatus(int id, RoomStatus status);
}
