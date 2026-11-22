package com.civicai.service;

import com.civicai.dto.*;
import com.civicai.entity.*;
import com.civicai.exception.BadRequestException;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.exception.UnauthorizedException;
import com.civicai.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderUpdateRepository updateRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            WorkOrderUpdateRepository updateRepository,
                            IncidentRepository incidentRepository,
                            UserRepository userRepository,
                            DepartmentRepository departmentRepository,
                            NotificationRepository notificationRepository,
                            AuditService auditService) {
        this.workOrderRepository = workOrderRepository;
        this.updateRepository = updateRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.notificationRepository = notificationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public WorkOrderResponseDto createWorkOrder(WorkOrderCreateDto dto, User user) {
        Incident incident = incidentRepository.findById(dto.getIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + dto.getIncidentId()));

        WorkOrder workOrder = new WorkOrder();
        workOrder.setIncident(incident);
        workOrder.setPriority(dto.getPriority() != null ? dto.getPriority().toUpperCase() : incident.getApprovedPriority());
        workOrder.setDescription(dto.getDescription());
        workOrder.setStatus("DRAFT");

        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(workOrder::setDepartment);
        } else if (incident.getAssignedDepartment() != null) {
            workOrder.setDepartment(incident.getAssignedDepartment());
        }

        if (dto.getAssignedInspectorId() != null) {
            userRepository.findById(dto.getAssignedInspectorId()).ifPresent(workOrder::setAssignedInspector);
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        // Update incident status
        incident.setStatus("WORK_ORDER_CREATED");
        incidentRepository.save(incident);

        // Log creation update
        WorkOrderUpdate initialUpdate = new WorkOrderUpdate();
        initialUpdate.setWorkOrder(saved);
        initialUpdate.setUpdatedBy(user);
        initialUpdate.setPreviousStatus(null);
        initialUpdate.setNewStatus("DRAFT");
        initialUpdate.setNotes("Work order drafted: " + (dto.getDescription() != null ? dto.getDescription() : ""));
        updateRepository.save(initialUpdate);

        auditService.logAction(user, "WORK_ORDER_CREATED", "WorkOrder", saved.getId(), "Draft created for incident #" + incident.getReferenceNumber(), null);

        return mapToDto(saved);
    }

    @Transactional
    public WorkOrderResponseDto approveWorkOrder(Long id, WorkOrderApprovalDto dto, User officer) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));

        String prev = workOrder.getStatus();
        workOrder.setStatus("APPROVED");
        workOrder.setApprovedBy(officer);

        WorkOrder saved = workOrderRepository.save(workOrder);

        WorkOrderUpdate update = new WorkOrderUpdate();
        update.setWorkOrder(saved);
        update.setUpdatedBy(officer);
        update.setPreviousStatus(prev);
        update.setNewStatus("APPROVED");
        update.setNotes(dto != null && dto.getNotes() != null ? dto.getNotes() : "Work order officially approved by Municipal Officer.");
        updateRepository.save(update);

        auditService.logAction(officer, "WORK_ORDER_APPROVED", "WorkOrder", saved.getId(), "Approved", null);

        return mapToDto(saved);
    }

    @Transactional
    public WorkOrderResponseDto assignWorkOrder(Long id, Long inspectorId, User officer) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));

        User inspector = userRepository.findById(inspectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspector user not found: " + inspectorId));

        if (inspector.getRole() != Role.FIELD_INSPECTOR) {
            throw new BadRequestException("Assigned user must have FIELD_INSPECTOR role.");
        }

        String prev = workOrder.getStatus();
        workOrder.setAssignedInspector(inspector);
        workOrder.setStatus("ASSIGNED");

        WorkOrder saved = workOrderRepository.save(workOrder);

        WorkOrderUpdate update = new WorkOrderUpdate();
        update.setWorkOrder(saved);
        update.setUpdatedBy(officer);
        update.setPreviousStatus(prev);
        update.setNewStatus("ASSIGNED");
        update.setNotes("Assigned to Field Inspector " + inspector.getName());
        updateRepository.save(update);

        // Notify Inspector
        Notification notif = new Notification(
                inspector,
                "Work Order Assigned",
                "You have been assigned to Work Order #" + saved.getId() + " (" + saved.getIncident().getCategory() + ")."
        );
        notificationRepository.save(notif);

        auditService.logAction(officer, "WORK_ORDER_ASSIGNED", "WorkOrder", saved.getId(), "Assigned to " + inspector.getEmail(), null);

        return mapToDto(saved);
    }

    @Transactional
    public WorkOrderResponseDto updateStatus(Long id, WorkOrderUpdateDto dto, User user) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));

        // Permission check: Field inspector can only update assigned work orders
        if (user.getRole() == Role.FIELD_INSPECTOR) {
            if (workOrder.getAssignedInspector() == null || !workOrder.getAssignedInspector().getId().equals(user.getId())) {
                throw new UnauthorizedException("Inspectors can only update work orders assigned to them.");
            }
        }

        String prev = workOrder.getStatus();
        String nextStatus = dto.getNewStatus().toUpperCase();
        workOrder.setStatus(nextStatus);

        // If marked RESOLVED, update associated Incident status as well
        if ("RESOLVED".equals(nextStatus)) {
            Incident incident = workOrder.getIncident();
            incident.setStatus("RESOLVED");
            incidentRepository.save(incident);

            // Notify citizen
            Notification notif = new Notification(
                    incident.getCitizen(),
                    "Incident Resolved",
                    "Great news! Your reported infrastructure damage #" + incident.getReferenceNumber() + " has been repaired and verified."
            );
            notificationRepository.save(notif);
        } else if ("IN_PROGRESS".equals(nextStatus)) {
            Incident incident = workOrder.getIncident();
            incident.setStatus("IN_PROGRESS");
            incidentRepository.save(incident);
        }

        WorkOrder saved = workOrderRepository.save(workOrder);

        WorkOrderUpdate update = new WorkOrderUpdate();
        update.setWorkOrder(saved);
        update.setUpdatedBy(user);
        update.setPreviousStatus(prev);
        update.setNewStatus(nextStatus);
        update.setNotes(dto.getNotes());
        update.setEvidenceFilename(dto.getEvidenceFilename());
        updateRepository.save(update);

        auditService.logAction(user, "WORK_ORDER_STATUS_CHANGED", "WorkOrder", saved.getId(),
                "Changed from " + prev + " to " + nextStatus, null);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderResponseDto> getWorkOrders(String status, String priority, Long departmentId, Pageable pageable) {
        return workOrderRepository.findFiltered(status, priority, departmentId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderResponseDto> getMyWorkOrders(User inspector, Pageable pageable) {
        return workOrderRepository.findByAssignedInspectorId(inspector.getId(), pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public WorkOrderResponseDto getWorkOrderById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found: " + id));
        return mapToDto(workOrder);
    }

    public WorkOrderResponseDto mapToDto(WorkOrder wo) {
        WorkOrderResponseDto dto = new WorkOrderResponseDto();
        dto.setId(wo.getId());
        dto.setIncidentId(wo.getIncident().getId());
        dto.setIncidentRef(wo.getIncident().getReferenceNumber());
        dto.setIncidentCategory(wo.getIncident().getCategory());
        dto.setIncidentAddress(wo.getIncident().getAddress());
        if (wo.getAssignedInspector() != null) {
            dto.setInspectorId(wo.getAssignedInspector().getId());
            dto.setInspectorName(wo.getAssignedInspector().getName());
        }
        if (wo.getDepartment() != null) {
            dto.setDepartmentId(wo.getDepartment().getId());
            dto.setDepartmentName(wo.getDepartment().getName());
        }
        dto.setPriority(wo.getPriority());
        dto.setDescription(wo.getDescription());
        dto.setStatus(wo.getStatus());
        if (wo.getApprovedBy() != null) {
            dto.setApprovedById(wo.getApprovedBy().getId());
            dto.setApprovedByName(wo.getApprovedBy().getName());
        }
        dto.setCreatedAt(wo.getCreatedAt());
        dto.setUpdatedAt(wo.getUpdatedAt());
        return dto;
    }
}
