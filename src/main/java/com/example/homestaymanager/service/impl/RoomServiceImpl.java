package com.example.homestaymanager.service.impl;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public Integer createRoom(Room room) {
        if (room.getNumber() <= 0) {
            throw new RuntimeException("Room number phải > 0");
        }

        if (room.getArea() <= 0) {
            throw new RuntimeException("Area phải > 0");
        }

        roomRepository.save(room);
        return room.getId();
    }

    @Override
    public Room getRoomByID(int id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    @Override
    public void deleteRoomById(int id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepository.delete(room);
    }
}