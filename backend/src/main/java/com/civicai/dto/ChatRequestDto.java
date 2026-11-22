package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequestDto {
    @NotBlank(message = "Message text is required")
    private String message;
    private String context;

    public ChatRequestDto() {}

    public ChatRequestDto(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
