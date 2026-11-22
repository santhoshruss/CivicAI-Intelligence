package com.civicai.service;

import com.civicai.dto.UserResponseDto;
import com.civicai.dto.UserRoleUpdateDto;
import com.civicai.entity.AuditLog;
import com.civicai.entity.Department;
import com.civicai.entity.Role;
import com.civicai.entity.User;
import com.civicai.exception.BadRequestException;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.repository.AuditLogRepository;
import com.civicai.repository.DepartmentRepository;
import com.civicai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository,
                        DepartmentRepository departmentRepository,
                        AuditLogRepository auditLogRepository,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, UserRoleUpdateDto dto, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        try {
            Role newRole = Role.valueOf(dto.getRole().toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + dto.getRole());
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));
            user.setDepartment(dept);
        }

        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }

        User saved = userRepository.save(user);

        auditService.logAction(admin, "USER_ROLE_UPDATED", "User", saved.getId(),
                "Updated role to " + saved.getRole() + ", enabled: " + saved.getEnabled(), null);

        return mapUserToDto(saved);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private UserResponseDto mapUserToDto(User u) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole().name());
        dto.setEnabled(u.getEnabled());
        dto.setCreatedAt(u.getCreatedAt());
        if (u.getDepartment() != null) {
            dto.setDepartmentId(u.getDepartment().getId());
            dto.setDepartmentName(u.getDepartment().getName());
        }
        return dto;
    }
}
