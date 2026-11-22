package com.civicai.repository;

import com.civicai.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Optional<WorkOrder> findByIncidentId(Long incidentId);
    Page<WorkOrder> findByAssignedInspectorId(Long inspectorId, Pageable pageable);
    List<WorkOrder> findByAssignedInspectorIdOrderByCreatedAtDesc(Long inspectorId);

    @Query("SELECT w FROM WorkOrder w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:priority IS NULL OR w.priority = :priority) AND " +
           "(:departmentId IS NULL OR w.department.id = :departmentId)")
    Page<WorkOrder> findFiltered(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );

    long countByStatus(String status);
}
