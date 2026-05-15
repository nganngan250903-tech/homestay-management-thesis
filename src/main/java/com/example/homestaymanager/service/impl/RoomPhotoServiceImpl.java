package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateRoomPhotoRequest;
import com.example.homestaymanager.model.RoomPhoto;
import com.example.homestaymanager.repository.RoomPhotoRepository;
import com.example.homestaymanager.service.RoomPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

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

    @Override
    public List<RoomPhoto> getListRoomPhoto() {
        return roomPhotoRepository.findAll();
    }

    @Override
    public RoomPhoto updateRoomPhotoById(int id, UpdateRoomPhotoRequest request) {
        RoomPhoto roomPhoto = roomPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomPhoto not found"));

        if (request.getPhoto() != null && !request.getPhoto().isBlank()) {
            roomPhoto.setPhoto(request.getPhoto());
        }

        return roomPhotoRepository.save(roomPhoto);
    }
}