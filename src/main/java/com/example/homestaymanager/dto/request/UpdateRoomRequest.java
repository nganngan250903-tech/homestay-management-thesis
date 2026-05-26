package com.example.homestaymanager.dto.request;
import com.example.homestaymanager.enums.RoomStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoomRequest {

    private Integer branchId;
    private Integer roomTypeId;
    private String name;
    private Integer number;       // dùng Integer để cho phép null
    private Float area;
    private String thumbnail;
    private RoomStatus status;
    private List<CreateRoomAmenityRequest> amenities;
}
