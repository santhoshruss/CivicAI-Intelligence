package com.civicai.controller;

import com.civicai.dto.ApiResponse;
import com.civicai.dto.UserResponseDto;
import com.civicai.dto.UserRoleUpdateDto;
import com.civicai.entity.AuditLog;
import com.civicai.entity.User;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User admin = userRepository.findById(userDetails.getId()).orElseThrow();
        UserResponseDto updated = adminService.updateUserRole(id, dto, admin);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logs = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
