package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateRoomTypeRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.model.RoomType;

import com.example.homestaymanager.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomTypeController {
    private final RoomTypeService roomTypeService;

    @GetMapping("/roomTypes")
    public ApiResponse<List<RoomType>> getListRoomType() {
        List<RoomType> roomTypes = roomTypeService.getListRoomType();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách loại phòng thành công", roomTypes);
    }

    @PostMapping("/roomTypes")
    public ApiResponse<Integer> createRoomType(@RequestBody RoomType roomType) {
        int i = roomTypeService.createRoomType(roomType);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, roomType.getId());
    }

    @GetMapping("/roomTypes/{roomTypeId}")
    public ApiResponse<RoomType> getRoomTypeById(@PathVariable int roomTypeId) {
        RoomType roomType = roomTypeService.getRoomTypeByID(roomTypeId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, roomType);
    }

    @PatchMapping("/roomTypes/{roomTypeId}")
    public ApiResponse<RoomType> updateRoomTypeById(@PathVariable int roomTypeId, @RequestBody UpdateRoomTypeRequest request) {
        RoomType roomType = roomTypeService.updateRoomTypeById(roomTypeId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, roomType);
    }

    @DeleteMapping("/roomTypes/{roomTypeId}")
    public ApiResponse<?> deleteRoomTypeById(@PathVariable int roomTypeId) {
        roomTypeService.deleteRoomTypeById(roomTypeId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
