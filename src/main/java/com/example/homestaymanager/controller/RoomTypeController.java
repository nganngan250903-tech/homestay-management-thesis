package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.RoomType;

import com.example.homestaymanager.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomTypeController {
    private final RoomTypeService roomTypeService;

    @PostMapping("/roomTypes")
    public Integer createRoomType(@RequestBody RoomType roomType) {
        return roomTypeService.createRoomType(roomType);
    }

    @GetMapping("/roomTypes/{roomTypeId}")
    public RoomType getRoomTypeById(@PathVariable int roomTypeId) {
        return roomTypeService.getRoomTypeByID(roomTypeId);
    }

    @DeleteMapping("/roomTypes/{roomTypeId}")
    public void deleteRoomTypeById(@PathVariable int roomTypeId) {
        roomTypeService.deleteRoomTypeById(roomTypeId);
    }
}
