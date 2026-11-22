package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRoleUpdateDto {
    @NotBlank(message = "Role is required")
    private String role;
    private Long departmentId;
    private Boolean enabled;

    public UserRoleUpdateDto() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
