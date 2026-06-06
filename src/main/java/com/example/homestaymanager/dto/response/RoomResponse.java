package com.example.homestaymanager.dto.response;

import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Branch;
import com.example.homestaymanager.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private int id;
    private Branch branch;
    private RoomType roomType;
    private String name;
    private Integer number;
    private float area;
    private String thumbnail;
    private RoomStatus status;
    private LocalDateTime cleaningStartedAt;
    private Boolean active;
    private List<RoomAmenityResponse> amenities;
}
