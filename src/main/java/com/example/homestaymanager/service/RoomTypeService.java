package com.example.homestaymanager.service;

import com.example.homestaymanager.model.RoomType;

public interface RoomTypeService {
    Integer createRoomType( RoomType roomType);
    RoomType getRoomTypeByID(int id);
    public void deleteRoomTypeById(int id);
}
