package com.civicai.repository;

import com.civicai.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByReferenceNumber(String referenceNumber);
    Page<Incident> findByCitizenId(Long citizenId, Pageable pageable);
    List<Incident> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    @Query("SELECT i FROM Incident i WHERE " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:priority IS NULL OR i.approvedPriority = :priority) AND " +
           "(:search IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.address) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Incident> findFiltered(
            @Param("category") String category,
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("search") String search,
            Pageable pageable
    );

    long countByStatus(String status);
    long countByApprovedPriority(String approvedPriority);
    long countByCategory(String category);

    @Query("SELECT i.category, COUNT(i) FROM Incident i GROUP BY i.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT i.approvedPriority, COUNT(i) FROM Incident i GROUP BY i.approvedPriority")
    List<Object[]> countGroupByPriority();

    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countGroupByStatus();

    List<Incident> findTop10ByOrderByCreatedAtDesc();

    // Query for nearby incidents (Haversine distance in meters approximated)
    @Query(value = "SELECT * FROM incidents WHERE id != :incidentId AND " +
                   "(6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(latitude)))) <= :radiusMeters",
           nativeQuery = true)
    List<Incident> findNearbyIncidents(
            @Param("incidentId") Long incidentId,
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters
    );
}
