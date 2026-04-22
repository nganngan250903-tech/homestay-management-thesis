package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.RoomPhoto;
import com.example.homestaymanager.repository.RoomPhotoRepository;
import com.example.homestaymanager.service.RoomPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomPhotoServiceImpl implements RoomPhotoService {

    private final RoomPhotoRepository roomPhotoRepository;

    @Override
    public Integer createRoomPhoto(RoomPhoto roomPhoto) {

        if (roomPhoto.getPhoto() == null || roomPhoto.getPhoto().trim().isEmpty()) {
            throw new RuntimeException("Photo không được để trống");
        }

        roomPhotoRepository.save(roomPhoto);
        return roomPhoto.getId();
    }

    @Override
    public RoomPhoto getRoomPhotoByID(int id) {
        return roomPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPhoto not found"));
    }

    @Override
    public void deleteRoomPhotoById(int id) {
        RoomPhoto roomPhoto = roomPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPhoto not found"));

        roomPhotoRepository.delete(roomPhoto);
    }
}