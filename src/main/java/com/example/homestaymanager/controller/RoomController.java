package com.example.homestaymanager.controller;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/rooms")
    public Integer createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @GetMapping("/rooms/{roomId}")
    public Room getRoomById(@PathVariable int roomId) {
        return roomService.getRoomByID(roomId);
    }

    @DeleteMapping("/rooms/{roomId}")
    public void deleteRoomById(@PathVariable int roomId) {
        roomService.deleteRoomById(roomId);
    }
}