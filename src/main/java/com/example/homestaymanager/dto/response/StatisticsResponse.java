package com.example.homestaymanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    private Map<String, BigDecimal> dailyRevenue;
    private Map<String, BigDecimal> monthlyRevenue;
    private Map<String, BigDecimal> yearlyRevenue;
    private long totalRooms;
    private long availableRooms;
    private long waitingCheckInRooms;
    private long occupiedRooms;
    private long cleaningRooms;
    private long maintenanceRooms;
    private long totalCustomers;
    private long activeCustomers;
    private long lockedCustomers;
    private long totalBookings;
    private Map<String, Long> bookingsByStatus;
    private String roomOccupancyMonth;
    private List<RoomOccupancyRevenue> roomOccupancyThisMonth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomOccupancyRevenue {
        private int roomId;
        private String roomName;
        private BigDecimal occupancyRate;
        private long occupiedNights;
        private long totalNights;
        private BigDecimal revenue;
    }
}
