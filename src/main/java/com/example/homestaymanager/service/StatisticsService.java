package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.response.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse getOverview(Integer occupancyYear, Integer occupancyMonth);
}
