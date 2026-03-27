package com.compliance.riskmonitor.controller;

import com.compliance.riskmonitor.dto.ApiResponse;
import com.compliance.riskmonitor.dto.DashboardSummary;
import com.compliance.riskmonitor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}