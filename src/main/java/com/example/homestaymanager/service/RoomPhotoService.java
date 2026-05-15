package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateRoomPhotoRequest;
import com.example.homestaymanager.model.RoomPhoto;
import java.util.List;

public interface RoomPhotoService {

    Integer createRoomPhoto(RoomPhoto roomPhoto);

    RoomPhoto getRoomPhotoByID(int id);

    void deleteRoomPhotoById(int id);

    List<RoomPhoto> getListRoomPhoto();

    RoomPhoto updateRoomPhotoById(int id, UpdateRoomPhotoRequest request);
}