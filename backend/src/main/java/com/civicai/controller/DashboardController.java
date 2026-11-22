package com.civicai.controller;

import com.civicai.dto.ApiResponse;
import com.civicai.dto.DashboardStatsDto;
import com.civicai.dto.IncidentResponseDto;
import com.civicai.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/recent-incidents")
    public ResponseEntity<ApiResponse<List<IncidentResponseDto>>> getRecentIncidents() {
        List<IncidentResponseDto> recent = dashboardService.getRecentIncidents();
        return ResponseEntity.ok(ApiResponse.success(recent));
    }

    @GetMapping("/category-stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCategoryStats() {
        Map<String, Long> stats = dashboardService.getCategoryStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/priority-stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPriorityStats() {
        Map<String, Long> stats = dashboardService.getPriorityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
