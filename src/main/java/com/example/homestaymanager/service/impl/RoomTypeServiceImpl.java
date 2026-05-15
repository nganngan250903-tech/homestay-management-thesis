package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateRoomTypeRequest;
import com.example.homestaymanager.model.Role;
import com.example.homestaymanager.model.RoomType;
import com.example.homestaymanager.repository.RoleRepository;
import com.example.homestaymanager.repository.RoomTypeRepository;
import com.example.homestaymanager.service.RoleService;
import com.example.homestaymanager.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    @Override
    public Integer createRoomType(RoomType roomType) {
        if(roomType.getMaxGuest()<1){
            throw new RuntimeException("maxGuest phải >= 1");
        }

        roomTypeRepository.save(roomType);
        return roomType.getId();
    }
    @Override
    public RoomType getRoomTypeByID(int id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));
    }

    @Override
    public void deleteRoomTypeById(int id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        roomTypeRepository.delete(roomType);
    }

    @Override
    public List<RoomType> getListRoomType() {
        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType updateRoomTypeById(int id, UpdateRoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            roomType.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            roomType.setDescription(request.getDescription());
        }

        if (request.getMaxGuest() != null && request.getMaxGuest() >= 1) {
            roomType.setMaxGuest(request.getMaxGuest());
        }

        if (request.getImage() != null && !request.getImage().isBlank()) {
            roomType.setImage(request.getImage());
        }

        return roomTypeRepository.save(roomType);
    }
}