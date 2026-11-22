package com.civicai.service;

import com.civicai.dto.*;
import com.civicai.entity.*;
import com.civicai.exception.BadRequestException;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.exception.UnauthorizedException;
import com.civicai.repository.DepartmentRepository;
import com.civicai.repository.EvidenceRepository;
import com.civicai.repository.IncidentRepository;
import com.civicai.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final DepartmentRepository departmentRepository;
    private final EvidenceRepository evidenceRepository;
    private final EvidenceService evidenceService;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    public IncidentService(IncidentRepository incidentRepository,
                           DepartmentRepository departmentRepository,
                           EvidenceRepository evidenceRepository,
                           EvidenceService evidenceService,
                           NotificationRepository notificationRepository,
                           AuditService auditService) {
        this.incidentRepository = incidentRepository;
        this.departmentRepository = departmentRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceService = evidenceService;
        this.notificationRepository = notificationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public IncidentResponseDto createIncident(IncidentCreateDto dto, User citizen) {
        Incident incident = new Incident();
        incident.setReferenceNumber(generateReferenceNumber());
        incident.setCitizen(citizen);
        incident.setCategory(dto.getCategory().toUpperCase());
        incident.setDescription(dto.getDescription());
        incident.setOriginalLanguage(dto.getOriginalLanguage() != null ? dto.getOriginalLanguage() : "English");
        incident.setNormalizedDescription(dto.getNormalizedDescription());
        incident.setLatitude(dto.getLatitude());
        incident.setLongitude(dto.getLongitude());
        incident.setAddress(dto.getAddress());
        incident.setStatus("REPORTED");
        incident.setRecommendedPriority("MEDIUM");
        incident.setApprovedPriority("MEDIUM");

        // Automatically associate with corresponding department code
        String deptCode = mapCategoryToDepartmentCode(dto.getCategory());
        departmentRepository.findByCode(deptCode).ifPresent(incident::setAssignedDepartment);

        Incident saved = incidentRepository.save(incident);

        // Create initial in-app notification for the citizen
        Notification notif = new Notification(
                citizen,
                "Incident Submitted",
                "Your incident report #" + saved.getReferenceNumber() + " (" + saved.getCategory() + ") was received."
        );
        notificationRepository.save(notif);

        auditService.logAction(citizen, "INCIDENT_REPORTED", "Incident", saved.getId(), "Report #" + saved.getReferenceNumber(), null);

        return mapToDto(saved);
    }

    public Page<IncidentResponseDto> getAllIncidents(String category, String status, String priority, String search, Pageable pageable) {
        Page<Incident> page = incidentRepository.findFiltered(category, status, priority, search, pageable);
        return page.map(this::mapToDto);
    }

    public Page<IncidentResponseDto> getMyIncidents(User citizen, Pageable pageable) {
        return incidentRepository.findByCitizenId(citizen.getId(), pageable).map(this::mapToDto);
    }

    public List<IncidentResponseDto> getAllIncidentsUnpaged() {
        return incidentRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public IncidentResponseDto getIncidentById(Long id, User currentUser) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        // CITIZEN can only view their own incidents
        if (currentUser.getRole() == Role.CITIZEN && !incident.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this incident.");
        }

        return mapToDto(incident);
    }

    @Transactional
    public IncidentResponseDto updateStatus(Long id, StatusUpdateDto dto, User officer) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        String oldStatus = incident.getStatus();
        incident.setStatus(dto.getStatus().toUpperCase());

        if (dto.getPriority() != null && !dto.getPriority().trim().isEmpty()) {
            incident.setApprovedPriority(dto.getPriority().toUpperCase());
        }

        Incident updated = incidentRepository.save(incident);

        // Notify citizen of progress
        Notification notif = new Notification(
                incident.getCitizen(),
                "Incident Status Update",
                "Your report #" + incident.getReferenceNumber() + " is now " + updated.getStatus() + "."
        );
        notificationRepository.save(notif);

        String details = "Status changed from " + oldStatus + " to " + updated.getStatus();
        if (dto.getExplanation() != null) {
            details += ". Note: " + dto.getExplanation();
        }
        auditService.logAction(officer, "INCIDENT_STATUS_UPDATED", "Incident", updated.getId(), details, null);

        return mapToDto(updated);
    }

    @Transactional
    public IncidentResponseDto uploadEvidence(Long incidentId, MultipartFile file, User user) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        if (user.getRole() == Role.CITIZEN && !incident.getCitizen().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only upload evidence to your own incident.");
        }

        evidenceService.saveEvidence(incident, file, user);
        auditService.logAction(user, "EVIDENCE_UPLOADED", "Incident", incident.getId(), "File: " + file.getOriginalFilename(), null);

        return mapToDto(incident);
    }

    public Incident getIncidentEntity(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
    }

    private String generateReferenceNumber() {
        int year = Year.now().getValue();
        int randomNum = 1000 + new Random().nextInt(9000);
        return "CIV-" + year + "-" + randomNum;
    }

    private String mapCategoryToDepartmentCode(String category) {
        if (category == null) return "ROADS_INFRA";
        switch (category.toUpperCase()) {
            case "POTHOLE":
            case "ROAD_CRACK":
                return "ROADS_INFRA";
            case "STREETLIGHT_DAMAGE":
                return "ELEC_LIGHT";
            case "WATER_LEAKAGE":
                return "WATER_DRAIN";
            case "GARBAGE_OVERFLOW":
                return "WASTE_MGMT";
            default:
                return "ROADS_INFRA";
        }
    }

    public IncidentResponseDto mapToDto(Incident incident) {
        IncidentResponseDto dto = new IncidentResponseDto();
        dto.setId(incident.getId());
        dto.setReferenceNumber(incident.getReferenceNumber());
        dto.setCitizenId(incident.getCitizen().getId());
        dto.setCitizenName(incident.getCitizen().getName());
        dto.setCitizenEmail(incident.getCitizen().getEmail());
        dto.setCategory(incident.getCategory());
        dto.setDescription(incident.getDescription());
        dto.setOriginalLanguage(incident.getOriginalLanguage());
        dto.setNormalizedDescription(incident.getNormalizedDescription());
        dto.setLatitude(incident.getLatitude());
        dto.setLongitude(incident.getLongitude());
        dto.setAddress(incident.getAddress());
        dto.setStatus(incident.getStatus());
        dto.setRecommendedPriority(incident.getRecommendedPriority());
        dto.setApprovedPriority(incident.getApprovedPriority());
        if (incident.getAssignedDepartment() != null) {
            dto.setAssignedDepartmentId(incident.getAssignedDepartment().getId());
            dto.setAssignedDepartmentName(incident.getAssignedDepartment().getName());
        }
        dto.setCreatedAt(incident.getCreatedAt());
        dto.setUpdatedAt(incident.getUpdatedAt());

        List<Evidence> evidenceList = evidenceRepository.findByIncidentId(incident.getId());
        List<String> filenames = evidenceList.stream()
                .map(Evidence::getStoredFilename)
                .collect(Collectors.toList());
        dto.setEvidenceFiles(filenames);

        return dto;
    }
}
