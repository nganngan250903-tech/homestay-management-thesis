package com.example.homestaymanager.service;

import com.example.homestaymanager.model.RoomPhoto;

public interface RoomPhotoService {

    Integer createRoomPhoto(RoomPhoto roomPhoto);

    RoomPhoto getRoomPhotoByID(int id);

    void deleteRoomPhotoById(int id);
}