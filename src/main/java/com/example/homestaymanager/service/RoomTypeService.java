package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateRoomTypeRequest;
import com.example.homestaymanager.model.RoomType;
import java.util.List;

public interface RoomTypeService {
    Integer createRoomType( RoomType roomType);
    RoomType getRoomTypeByID(int id);
    public void deleteRoomTypeById(int id);
    List<RoomType> getListRoomType();
    RoomType updateRoomTypeById(int id, UpdateRoomTypeRequest request);
}
