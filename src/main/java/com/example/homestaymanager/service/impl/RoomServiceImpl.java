package com.example.homestaymanager.service.impl;
import com.example.homestaymanager.dto.request.UpdateRoomRequest;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.repository.BranchRepository;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.repository.RoomTypeRepository;
import com.example.homestaymanager.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final RoomTypeRepository roomTypeRepository;

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

    @Override
    public List<Room> getListRoom() {
        return roomRepository.findAll();
    }

    @Override
    public Room updateRoomById(int id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getBranchId() != null) {
            room.setBranch(branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found")));
        }

        if (request.getRoomTypeId() != null) {
            room.setRoomType(roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("RoomType not found")));
        }

        if (request.getNumber() != null && request.getNumber() > 0) {
            room.setNumber(request.getNumber());
        }

        if (request.getArea() != null && request.getArea() > 0) {
            room.setArea(request.getArea());
        }

        if (request.getThumbnail() != null && !request.getThumbnail().isBlank()) {
            room.setThumbnail(request.getThumbnail());
        }

        return roomRepository.save(room);
    }
}