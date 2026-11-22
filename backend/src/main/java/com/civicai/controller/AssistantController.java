package com.civicai.controller;

import com.civicai.dto.ApiResponse;
import com.civicai.dto.ChatRequestDto;
import com.civicai.dto.ChatResponseDto;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponseDto>> chat(
            @Valid @RequestBody ChatRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String role = userDetails != null ? userDetails.getRole() : "CITIZEN";
        ChatResponseDto response = assistantService.processMessage(request, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
