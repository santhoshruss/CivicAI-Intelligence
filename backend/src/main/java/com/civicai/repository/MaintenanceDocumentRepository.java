package com.civicai.repository;

import com.civicai.entity.MaintenanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceDocumentRepository extends JpaRepository<MaintenanceDocument, Long> {
    List<MaintenanceDocument> findByCategory(String category);
    List<MaintenanceDocument> findAllByOrderByCreatedAtDesc();
}
