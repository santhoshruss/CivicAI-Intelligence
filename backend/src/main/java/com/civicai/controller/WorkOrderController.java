package com.civicai.controller;

import com.civicai.dto.*;
import com.civicai.entity.Role;
import com.civicai.entity.User;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final UserRepository userRepository;

    public WorkOrderController(WorkOrderService workOrderService, UserRepository userRepository) {
        this.workOrderService = workOrderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponseDto>> createWorkOrder(
            @Valid @RequestBody WorkOrderCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        WorkOrderResponseDto response = workOrderService.createWorkOrder(dto, user);
        return ResponseEntity.ok(ApiResponse.success("Work order created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WorkOrderResponseDto>>> getWorkOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WorkOrderResponseDto> response;
        if (user.getRole() == Role.FIELD_INSPECTOR) {
            response = workOrderService.getMyWorkOrders(user, pageable);
        } else {
            response = workOrderService.getWorkOrders(status, priority, departmentId, pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponseDto>> getWorkOrderById(@PathVariable Long id) {
        WorkOrderResponseDto response = workOrderService.getWorkOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponseDto>> approveWorkOrder(
            @PathVariable Long id,
            @RequestBody(required = false) WorkOrderApprovalDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User officer = userRepository.findById(userDetails.getId()).orElseThrow();
        WorkOrderResponseDto response = workOrderService.approveWorkOrder(id, dto, officer);
        return ResponseEntity.ok(ApiResponse.success("Work order officially approved", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WorkOrderResponseDto>> assignWorkOrder(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderAssignDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User officer = userRepository.findById(userDetails.getId()).orElseThrow();
        WorkOrderResponseDto response = workOrderService.assignWorkOrder(id, dto.getInspectorId(), officer);
        return ResponseEntity.ok(ApiResponse.success("Work order assigned to inspector", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WorkOrderResponseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        WorkOrderResponseDto response = workOrderService.updateStatus(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success("Work order status updated", response));
    }
}
