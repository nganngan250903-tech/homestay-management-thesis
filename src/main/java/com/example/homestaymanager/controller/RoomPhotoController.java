package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateRoomPhotoRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.RoomPhoto;
import com.example.homestaymanager.service.RoomPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomPhotoController {

    private final RoomPhotoService roomPhotoService;

    @GetMapping("/roomPhotos")
    public ApiResponse<List<RoomPhoto>> getListRoomPhoto() {
        List<RoomPhoto> roomPhotos = roomPhotoService.getListRoomPhoto();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách ảnh phòng thành công", roomPhotos);
    }

    @PostMapping("/roomPhotos")
    public ApiResponse<Integer> createRoomPhoto(@RequestBody RoomPhoto roomPhoto) {
        int i = roomPhotoService.createRoomPhoto(roomPhoto);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, roomPhoto.getId());
    }

    @GetMapping("/roomPhotos/{roomPhotoId}")
    public ApiResponse<RoomPhoto> getRoomPhotoById(@PathVariable int roomPhotoId) {
        RoomPhoto roomPhoto = roomPhotoService.getRoomPhotoByID(roomPhotoId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, roomPhoto);
    }

    @PatchMapping("/roomPhotos/{roomPhotoId}")
    public ApiResponse<RoomPhoto> updateRoomPhotoById(@PathVariable int roomPhotoId, @RequestBody UpdateRoomPhotoRequest request) {
        RoomPhoto roomPhoto = roomPhotoService.updateRoomPhotoById(roomPhotoId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, roomPhoto);
    }

    @DeleteMapping("/roomPhotos/{roomPhotoId}")
    public ApiResponse<?> deleteRoomPhotoById(@PathVariable int roomPhotoId) {
        roomPhotoService.deleteRoomPhotoById(roomPhotoId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}