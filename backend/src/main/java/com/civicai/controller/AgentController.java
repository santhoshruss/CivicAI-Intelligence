package com.civicai.controller;

import com.civicai.dto.AgentRunDto;
import com.civicai.dto.ApiResponse;
import com.civicai.entity.User;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final UserRepository userRepository;

    public AgentController(AgentService agentService, UserRepository userRepository) {
        this.agentService = agentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/analyze/{incidentId}")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AgentRunDto>> runAgentWorkflow(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        AgentRunDto response = agentService.executeIncidentTriageWorkflow(incidentId, user);
        return ResponseEntity.ok(ApiResponse.success("Agentic AI triage completed successfully", response));
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<ApiResponse<AgentRunDto>> getAgentRun(@PathVariable Long id) {
        AgentRunDto response = agentService.getAgentRun(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/runs/incident/{incidentId}")
    public ResponseEntity<ApiResponse<AgentRunDto>> getLatestAgentRunForIncident(@PathVariable Long incidentId) {
        AgentRunDto response = agentService.getLatestAgentRunForIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
