package com.civicai.service;

import com.civicai.dto.DashboardStatsDto;
import com.civicai.dto.IncidentResponseDto;
import com.civicai.repository.IncidentRepository;
import com.civicai.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final IncidentRepository incidentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final IncidentService incidentService;

    public DashboardService(IncidentRepository incidentRepository,
                            WorkOrderRepository workOrderRepository,
                            IncidentService incidentService) {
        this.incidentRepository = incidentRepository;
        this.workOrderRepository = workOrderRepository;
        this.incidentService = incidentService;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long total = incidentRepository.count();
        long reported = incidentRepository.countByStatus("REPORTED");
        long underReview = incidentRepository.countByStatus("UNDER_REVIEW");
        long aiAnalyzed = incidentRepository.countByStatus("AI_ANALYZED");
        long resolved = incidentRepository.countByStatus("RESOLVED");

        long high = incidentRepository.countByApprovedPriority("HIGH");
        long critical = incidentRepository.countByApprovedPriority("CRITICAL");

        long activeOrders = workOrderRepository.count() - workOrderRepository.countByStatus("RESOLVED");

        stats.setTotalIncidents(total);
        stats.setPendingIncidents(reported + underReview + aiAnalyzed);
        stats.setHighPriorityIncidents(high + critical);
        stats.setActiveWorkOrders(Math.max(0, activeOrders));
        stats.setResolvedIncidents(resolved);

        // Category breakdown
        Map<String, Long> catMap = new HashMap<>();
        for (Object[] row : incidentRepository.countGroupByCategory()) {
            catMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        stats.setCategoryBreakdown(catMap);

        // Priority breakdown
        Map<String, Long> prioMap = new HashMap<>();
        for (Object[] row : incidentRepository.countGroupByPriority()) {
            prioMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        stats.setPriorityBreakdown(prioMap);

        return stats;
    }

    public List<IncidentResponseDto> getRecentIncidents() {
        return incidentRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(incidentService::mapToDto)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getCategoryStats() {
        Map<String, Long> catMap = new HashMap<>();
        for (Object[] row : incidentRepository.countGroupByCategory()) {
            catMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return catMap;
    }

    public Map<String, Long> getPriorityStats() {
        Map<String, Long> prioMap = new HashMap<>();
        for (Object[] row : incidentRepository.countGroupByPriority()) {
            prioMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return prioMap;
    }
}
