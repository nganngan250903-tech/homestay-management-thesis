package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.StatisticsResponse;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/statistics/overview")
    public ApiResponse<StatisticsResponse> getOverview(
            @RequestParam(required = false) Integer occupancyYear,
            @RequestParam(required = false) Integer occupancyMonth) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admin can view statistics");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, statisticsService.getOverview(occupancyYear, occupancyMonth));
    }
}
