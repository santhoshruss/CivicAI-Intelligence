package com.civicai.dto;

import java.util.Map;

public class DashboardStatsDto {
    private long totalIncidents;
    private long pendingIncidents;
    private long highPriorityIncidents;
    private long activeWorkOrders;
    private long resolvedIncidents;
    private Map<String, Long> categoryBreakdown;
    private Map<String, Long> priorityBreakdown;

    public DashboardStatsDto() {}

    public long getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(long totalIncidents) { this.totalIncidents = totalIncidents; }

    public long getPendingIncidents() { return pendingIncidents; }
    public void setPendingIncidents(long pendingIncidents) { this.pendingIncidents = pendingIncidents; }

    public long getHighPriorityIncidents() { return highPriorityIncidents; }
    public void setHighPriorityIncidents(long highPriorityIncidents) { this.highPriorityIncidents = highPriorityIncidents; }

    public long getActiveWorkOrders() { return activeWorkOrders; }
    public void setActiveWorkOrders(long activeWorkOrders) { this.activeWorkOrders = activeWorkOrders; }

    public long getResolvedIncidents() { return resolvedIncidents; }
    public void setResolvedIncidents(long resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }

    public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public Map<String, Long> getPriorityBreakdown() { return priorityBreakdown; }
    public void setPriorityBreakdown(Map<String, Long> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; }
}
