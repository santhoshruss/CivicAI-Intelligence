package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DuplicateConfirmDto {
    @NotNull(message = "Duplicate record ID is required")
    private Long duplicateId;

    @NotBlank(message = "Action is required (CONFIRMED_DUPLICATE or DISMISSED)")
    private String action; // CONFIRMED_DUPLICATE, DISMISSED

    public DuplicateConfirmDto() {}

    public Long getDuplicateId() { return duplicateId; }
    public void setDuplicateId(Long duplicateId) { this.duplicateId = duplicateId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
