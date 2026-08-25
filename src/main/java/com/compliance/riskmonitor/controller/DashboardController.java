package com.compliance.riskmonitor.controller;

import com.compliance.riskmonitor.dto.*;
import com.compliance.riskmonitor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/v1/dashboard/summary
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        log.info("GET /api/v1/dashboard/summary");
        DashboardSummary summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // GET /api/v1/dashboard/risk-breakdown
    @GetMapping("/risk-breakdown")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRiskBreakdown() {
        log.info("GET /api/v1/dashboard/risk-breakdown");
        Map<String, Long> breakdown = dashboardService.getRiskBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }
    // GET /api/v1/dashboard/trends/daily
    @GetMapping("/trends/daily")
    public ResponseEntity<ApiResponse<List<DailyTrendDTO>>> getDailyTrends() {
        log.info("GET /api/v1/dashboard/trends/daily");
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getDailyTrends())
        );
    }

    // GET /api/v1/dashboard/trends/hourly
    @GetMapping("/trends/hourly")
    public ResponseEntity<ApiResponse<List<HourlyPatternDTO>>> getHourlyPatterns() {
        log.info("GET /api/v1/dashboard/trends/hourly");
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getHourlyPatterns())
        );
    }

    // GET /api/v1/dashboard/locations
    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<List<LocationAnalyticsDTO>>> getLocationAnalytics() {
        log.info("GET /api/v1/dashboard/locations");
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getLocationAnalytics())
        );
    }
}