package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.CreateRoomRequest;
import com.example.homestaymanager.dto.request.UpdateRoomRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.RoomResponse;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/rooms")
    public ApiResponse<List<RoomResponse>> getListRoom() {
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, roomService.getListRoom());
    }

    @PostMapping("/rooms")
    public ApiResponse<Integer> createRoom(@RequestBody CreateRoomRequest request) {
        int id = roomService.createRoom(request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, id);
    }

    @GetMapping("/rooms/{roomId}")
    public ApiResponse<RoomResponse> getRoomById(@PathVariable int roomId) {
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, roomService.getRoomByID(roomId));
    }

    @PatchMapping("/rooms/{roomId}")
    public ApiResponse<RoomResponse> updateRoomById(@PathVariable int roomId, @RequestBody UpdateRoomRequest request) {
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, roomService.updateRoomById(roomId, request));
    }

    @PatchMapping("/rooms/{roomId}/status")
    public ApiResponse<RoomResponse> updateRoomStatus(@PathVariable int roomId, @RequestBody UpdateRoomRequest request) {
        if (!SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Only staff can update room status");
        }
        if (request == null || request.getStatus() == null) {
            return ApiResponse.of(ApiStatus.BAD_REQUEST, "Room status is required", null);
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, roomService.updateRoomStatus(roomId, request.getStatus()));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ApiResponse<?> deleteRoomById(@PathVariable int roomId) {
        roomService.deleteRoomById(roomId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
