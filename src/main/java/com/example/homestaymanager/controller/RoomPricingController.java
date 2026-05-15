package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateRoomPricingRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.RoomPricing;
import com.example.homestaymanager.service.RoomPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomPricingController {

    private final RoomPricingService roomPricingService;

    @GetMapping("/roomPricings")
    public ApiResponse<List<RoomPricing>> getListRoomPricing() {
        List<RoomPricing> roomPricings = roomPricingService.getListRoomPricing();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách giá phòng thành công", roomPricings);
    }

    @PostMapping("/roomPricings")
    public ApiResponse<Integer> createRoomPricing(@RequestBody RoomPricing roomPricing) {
        int i = roomPricingService.createRoomPricing(roomPricing);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, roomPricing.getId());
    }

    @GetMapping("/roomPricings/{roomPricingId}")
    public ApiResponse<RoomPricing> getRoomPricingById(@PathVariable int roomPricingId) {
        RoomPricing roomPricing = roomPricingService.getRoomPricingByID(roomPricingId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, roomPricing);
    }

    @PatchMapping("/roomPricings/{roomPricingId}")
    public ApiResponse<RoomPricing> updateRoomPricingById(@PathVariable int roomPricingId, @RequestBody UpdateRoomPricingRequest request) {
        RoomPricing roomPricing = roomPricingService.updateRoomPricingById(roomPricingId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, roomPricing);
    }

    @DeleteMapping("/roomPricings/{roomPricingId}")
    public ApiResponse<?> deleteRoomPricingById(@PathVariable int roomPricingId) {
        roomPricingService.deleteRoomPricingById(roomPricingId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}