package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.RoomPhoto;
import com.example.homestaymanager.service.RoomPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomPhotoController {

    private final RoomPhotoService roomPhotoService;

    @PostMapping("/roomPhotos")
    public Integer createRoomPhoto(@RequestBody RoomPhoto roomPhoto) {
        return roomPhotoService.createRoomPhoto(roomPhoto);
    }

    @GetMapping("/roomPhotos/{roomPhotoId}")
    public RoomPhoto getRoomPhotoById(@PathVariable int roomPhotoId) {
        return roomPhotoService.getRoomPhotoByID(roomPhotoId);
    }

    @DeleteMapping("/roomPhotos/{roomPhotoId}")
    public void deleteRoomPhotoById(@PathVariable int roomPhotoId) {
        roomPhotoService.deleteRoomPhotoById(roomPhotoId);
    }
}