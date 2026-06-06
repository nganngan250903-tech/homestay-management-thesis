package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.CreateRoomAmenityRequest;
import com.example.homestaymanager.dto.request.CreateRoomRequest;
import com.example.homestaymanager.dto.request.UpdateRoomRequest;
import com.example.homestaymanager.dto.response.RoomAmenityResponse;
import com.example.homestaymanager.dto.response.RoomResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.exception.BadRequestException;
import com.example.homestaymanager.model.Amenity;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.model.RoomAmenity;
import com.example.homestaymanager.repository.AmenityRepository;
import com.example.homestaymanager.repository.BranchRepository;
import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.RoomAmenityRepository;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.repository.RoomTypeRepository;
import com.example.homestaymanager.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final RoomAmenityRepository roomAmenityRepository;

    @Override
    @Transactional
    public Integer createRoom(CreateRoomRequest request) {
        if (request == null) {
            throw new BadRequestException("Thông tin phòng là bắt buộc");
        }
        if (request.getBranchId() == null) {
            throw new BadRequestException("Vui lòng chọn chi nhánh");
        }
        if (request.getRoomTypeId() == null) {
            throw new BadRequestException("Vui lòng chọn loại phòng");
        }
        if (request.getNumber() == null || request.getNumber() <= 0) {
            throw new BadRequestException("Vui lòng nhập số phòng lớn hơn 0");
        }
        if (request.getArea() <= 0) {
            throw new BadRequestException("Diện tích phải lớn hơn 0");
        }

        Room room = new Room();
        room.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found")));
        room.setRoomType(roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found")));
        room.setName(resolveRoomName(request.getName()));
        room.setNumber(request.getNumber());
        room.setArea(request.getArea());
        room.setThumbnail(request.getThumbnail());
        applyRoomStatus(room, request.getStatus() != null ? request.getStatus() : RoomStatus.AVAILABLE);

        roomRepository.save(room);
        replaceAmenities(room, request.getAmenities());
        return room.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomByID(int id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return toResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoomById(int id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setActive(false);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getListRoom() {
        return roomRepository.findVisibleRooms().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public RoomResponse updateRoomById(int id, UpdateRoomRequest request) {
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

        if (request.getName() != null && !request.getName().isBlank()) {
            room.setName(request.getName().trim());
        } else if (room.getName() == null || room.getName().isBlank()) {
            room.setName(resolveRoomName(null));
        }

        if (request.getArea() != null && request.getArea() > 0) {
            room.setArea(request.getArea());
        }

        if (request.getThumbnail() != null) {
            room.setThumbnail(request.getThumbnail());
        }

        if (request.getStatus() != null) {
            applyRoomStatus(room, resolveManualRoomStatus(room.getId(), request.getStatus()));
        }

        Room saved = roomRepository.save(room);
        if (request.getAmenities() != null) {
            replaceAmenities(saved, request.getAmenities());
        }
        return toResponse(saved);
    }

    private void replaceAmenities(Room room, List<CreateRoomAmenityRequest> amenities) {
        if (amenities == null) {
            return;
        }
        roomAmenityRepository.deleteByRoomId(room.getId());
        for (CreateRoomAmenityRequest item : amenities) {
            if (item.getAmenityId() == null) {
                continue;
            }
            Amenity amenity = amenityRepository.findById(item.getAmenityId())
                    .orElseThrow(() -> new RuntimeException("Amenity not found"));
            RoomAmenity roomAmenity = new RoomAmenity();
            roomAmenity.setRoom(room);
            roomAmenity.setAmenity(amenity);
            roomAmenity.setQuantity(item.getQuantity() > 0 ? item.getQuantity() : 1);
            roomAmenityRepository.save(roomAmenity);
        }
    }

    private RoomResponse toResponse(Room room) {
        List<RoomAmenityResponse> amenities = roomAmenityRepository.findByRoomId(room.getId())
                .stream()
                .map(item -> RoomAmenityResponse.builder()
                        .id(item.getId())
                        .amenityId(item.getAmenity().getId())
                        .amenityName(item.getAmenity().getName())
                        .amenityDescription(null)
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return RoomResponse.builder()
                .id(room.getId())
                .branch(room.getBranch())
                .roomType(room.getRoomType())
                .name(resolveRoomName(room.getName()))
                .number(room.getNumber())
                .area(room.getArea())
                .thumbnail(room.getThumbnail())
                .status(room.getStatus() != null ? room.getStatus() : RoomStatus.AVAILABLE)
                .cleaningStartedAt(room.getCleaningStartedAt())
                .active(room.getActive() == null || room.getActive())
                .amenities(amenities)
                .build();
    }

    @Override
    @Transactional
    public RoomResponse updateRoomStatus(int id, RoomStatus status) {
        if (status == null) {
            throw new RuntimeException("Room status is required");
        }
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        applyRoomStatus(room, resolveManualRoomStatus(room.getId(), status));
        return toResponse(roomRepository.save(room));
    }

    private void applyRoomStatus(Room room, RoomStatus status) {
        room.setStatus(status);
        if (status == RoomStatus.CLEANING) {
            if (room.getCleaningStartedAt() == null) {
                room.setCleaningStartedAt(LocalDateTime.now());
            }
        } else {
            room.setCleaningStartedAt(null);
        }
    }

    private String resolveRoomName(String name) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return "Phòng";
    }

    private RoomStatus resolveManualRoomStatus(int roomId, RoomStatus requestedStatus) {
        if (requestedStatus != RoomStatus.AVAILABLE) {
            return requestedStatus;
        }

        LocalDateTime now = LocalDateTime.now();
        if (bookingRepository
                .findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNotNullAndActualCheckOutAtIsNullOrderByActualCheckInAtDesc(
                        roomId,
                        BookingStatus.CONFIRMED)
                .isPresent()) {
            return RoomStatus.OCCUPIED;
        }
        if (bookingRepository
                .findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNullAndActualCheckOutAtIsNullAndCheckInLessThanEqualAndCheckOutAfterOrderByCheckInAsc(
                        roomId,
                        BookingStatus.CONFIRMED,
                        now,
                        now)
                .isPresent()) {
            return RoomStatus.WAITING_CHECKIN;
        }
        return RoomStatus.AVAILABLE;
    }
}
