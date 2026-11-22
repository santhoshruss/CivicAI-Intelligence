package com.civicai.service;

import com.civicai.dto.*;
import com.civicai.entity.*;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.repository.*;
import com.civicai.util.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final IncidentRepository incidentRepository;
    private final AgentRunRepository agentRunRepository;
    private final AgentToolLogRepository toolLogRepository;
    private final AnalysisService analysisService;
    private final DuplicateDetectionService duplicateService;
    private final KnowledgeService knowledgeService;
    private final WorkOrderService workOrderService;
    private final DepartmentRepository departmentRepository;
    private final EvidenceRepository evidenceRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    @Value("${civicai.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public AgentService(IncidentRepository incidentRepository,
                        AgentRunRepository agentRunRepository,
                        AgentToolLogRepository toolLogRepository,
                        AnalysisService analysisService,
                        DuplicateDetectionService duplicateService,
                        KnowledgeService knowledgeService,
                        WorkOrderService workOrderService,
                        DepartmentRepository departmentRepository,
                        EvidenceRepository evidenceRepository,
                        FileStorageService fileStorageService,
                        AuditService auditService) {
        this.incidentRepository = incidentRepository;
        this.agentRunRepository = agentRunRepository;
        this.toolLogRepository = toolLogRepository;
        this.analysisService = analysisService;
        this.duplicateService = duplicateService;
        this.knowledgeService = knowledgeService;
        this.workOrderService = workOrderService;
        this.departmentRepository = departmentRepository;
        this.evidenceRepository = evidenceRepository;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public AgentRunDto executeIncidentTriageWorkflow(Long incidentId, User officer) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        AgentRun agentRun = new AgentRun();
        agentRun.setIncident(incident);
        agentRun.setExecutionStatus("RUNNING");
        agentRun.setStartedAt(LocalDateTime.now());
        AgentRun savedRun = agentRunRepository.save(agentRun);

        List<AgentToolLog> toolLogs = new ArrayList<>();

        // Try Python AI Agent endpoint first
        boolean pythonAgentSuccess = false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> req = new HashMap<>();
            req.put("incident_id", incident.getId());
            req.put("reference_number", incident.getReferenceNumber());
            req.put("category", incident.getCategory());
            req.put("description", incident.getDescription());
            req.put("latitude", incident.getLatitude());
            req.put("longitude", incident.getLongitude());
            req.put("address", incident.getAddress());

            // 1. Pass REAL evidence image bytes (Base64)
            List<Evidence> evList = evidenceRepository.findByIncidentId(incident.getId());
            if (!evList.isEmpty()) {
                try {
                    Path p = fileStorageService.getEvidenceStorageLocation().resolve(evList.get(0).getStoredFilename());
                    if (Files.exists(p)) {
                        byte[] bytes = Files.readAllBytes(p);
                        req.put("image_base64", Base64.getEncoder().encodeToString(bytes));
                        req.put("image_path", p.toString());
                    }
                } catch (Exception ex) {
                    log.warn("Could not read evidence bytes for agent: {}", ex.getMessage());
                }
            }

            // 2. Pass REAL nearby candidate incidents from database within 300m
            List<Incident> nearby = incidentRepository.findNearbyIncidents(
                    incident.getId(),
                    incident.getLatitude().doubleValue(),
                    incident.getLongitude().doubleValue(),
                    300.0
            );
            List<Map<String, Object>> candidateList = nearby.stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("reference_number", c.getReferenceNumber());
                m.put("category", c.getCategory());
                m.put("description", c.getDescription());
                m.put("latitude", c.getLatitude().doubleValue());
                m.put("longitude", c.getLongitude().doubleValue());
                return m;
            }).collect(Collectors.toList());
            req.put("candidates", candidateList);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    aiServiceUrl + "/api/agent/run",
                    entity,
                    Map.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<String, Object> data = resp.getBody();
                String rec = (String) data.getOrDefault("recommendation", "");
                String recPriority = (String) data.getOrDefault("recommended_priority", "MEDIUM");
                savedRun.setRecommendation(rec);
                savedRun.setExecutionStatus("COMPLETED");
                savedRun.setCompletedAt(LocalDateTime.now());

                incident.setRecommendedPriority(recPriority);
                incidentRepository.save(incident);

                Object rawLogs = data.get("tool_logs");
                if (rawLogs instanceof List) {
                    for (Object l : (List<?>) rawLogs) {
                        if (l instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> lm = (Map<String, Object>) l;
                            AgentToolLog logEntry = new AgentToolLog();
                            logEntry.setAgentRun(savedRun);
                            Object toolNameVal = lm.get("tool_name");
                            logEntry.setToolName(toolNameVal != null ? toolNameVal.toString() : "tool");
                            Object inVal = lm.get("input_summary");
                            logEntry.setInputSummary(inVal != null ? inVal.toString() : "");
                            Object outVal = lm.get("output_summary");
                            logEntry.setOutputSummary(outVal != null ? outVal.toString() : "");
                            logEntry.setExecutionStatus("SUCCESS");
                            toolLogs.add(toolLogRepository.save(logEntry));
                        }
                    }
                }

                // Stage Draft Work Order if not already present
                try {
                    WorkOrderCreateDto woDto = new WorkOrderCreateDto();
                    woDto.setIncidentId(incident.getId());
                    woDto.setPriority(recPriority);
                    woDto.setDescription("Agent Intelligence Draft Work Order: " + incident.getCategory() + " repair at " + incident.getAddress());
                    if (incident.getAssignedDepartment() != null) {
                        woDto.setDepartmentId(incident.getAssignedDepartment().getId());
                    }
                    workOrderService.createWorkOrder(woDto, officer);
                } catch (Exception ex) {
                    log.info("Work order draft stage notice: {}", ex.getMessage());
                }

                pythonAgentSuccess = true;
            }
        } catch (Exception ex) {
            log.warn("[AgentService] Python agent workflow unavailable: {}. Executing native tool pipeline.", ex.getMessage());
        }

        if (!pythonAgentSuccess) {
            // Native Rule-based Tool Pipeline
            // Tool 1: get_incident_details
            AgentToolLog t1 = logTool(savedRun, "get_incident_details",
                    "id=" + incident.getId() + ", ref=" + incident.getReferenceNumber(),
                    "Retrieved incident details: " + incident.getCategory() + " at " + incident.getAddress());
            toolLogs.add(t1);

            // Tool 2: analyze_damage
            AnalysisResponseDto analysisDto = null;
            try {
                analysisDto = analysisService.getLatestAnalysis(incident.getId());
            } catch (Exception e) {
                analysisDto = analysisService.analyzeIncident(incident.getId(), officer);
            }
            AgentToolLog t2 = logTool(savedRun, "analyze_damage",
                    "category=" + incident.getCategory() + ", evidence_count=" + (incident.getCategory()),
                    "Model: " + analysisDto.getModelName() + ", Confidence: " + analysisDto.getConfidence() + ", Pred: " + analysisDto.getPredictedCategory());
            toolLogs.add(t2);

            // Tool 3: find_nearby_duplicates
            List<DuplicateResponseDto> dups = duplicateService.findAndRecordDuplicates(incident.getId());
            AgentToolLog t3 = logTool(savedRun, "find_nearby_duplicates",
                    "lat=" + incident.getLatitude() + ", lon=" + incident.getLongitude() + ", radius=300m",
                    "Found " + dups.size() + " potential duplicate incident(s) in immediate vicinity.");
            toolLogs.add(t3);

            // Tool 4: retrieve_maintenance_guidelines
            KnowledgeQueryDto kq = new KnowledgeQueryDto();
            kq.setQuery(incident.getCategory() + " repair procedure and safety protocol");
            kq.setCategory(incident.getCategory());
            KnowledgeResponseDto kr = knowledgeService.queryKnowledge(kq);
            String citationSummary = kr.getSources().isEmpty() ? "Standard Municipal Code" :
                    kr.getSources().get(0).getDocumentName() + " (p. " + kr.getSources().get(0).getPageNumber() + ")";
            AgentToolLog t4 = logTool(savedRun, "retrieve_maintenance_guidelines",
                    "query=" + kq.getQuery(),
                    "Retrieved guideline citation: " + citationSummary);
            toolLogs.add(t4);

            // Tool 5: calculate_priority
            String calculatedPriority = calculatePriority(incident, analysisDto.getConfidence(), dups.size());
            AgentToolLog t5 = logTool(savedRun, "calculate_priority",
                    "category=" + incident.getCategory() + ", dups=" + dups.size() + ", conf=" + analysisDto.getConfidence(),
                    "Calculated Recommended Priority: " + calculatedPriority);
            toolLogs.add(t5);

            // Tool 6: generate_recommendation
            String recommendation = buildRecommendation(incident, calculatedPriority, citationSummary, dups.size());
            AgentToolLog t6 = logTool(savedRun, "generate_recommendation",
                    "priority=" + calculatedPriority,
                    recommendation);
            toolLogs.add(t6);

            // Tool 7: create_draft_work_order
            WorkOrderCreateDto woDto = new WorkOrderCreateDto();
            woDto.setIncidentId(incident.getId());
            woDto.setPriority(calculatedPriority);
            woDto.setDescription("Agent Drafted Work Order: " + incident.getCategory() + " repair at " + incident.getAddress() + ". Follow " + citationSummary);
            if (incident.getAssignedDepartment() != null) {
                woDto.setDepartmentId(incident.getAssignedDepartment().getId());
            }
            WorkOrderResponseDto draftWo = workOrderService.createWorkOrder(woDto, officer);
            AgentToolLog t7 = logTool(savedRun, "create_draft_work_order",
                    "incidentId=" + incident.getId() + ", priority=" + calculatedPriority,
                    "Created Work Order #" + draftWo.getId() + " in DRAFT status awaiting officer approval.");
            toolLogs.add(t7);

            // Tool 8: generate_incident_summary
            String summary = "Triage completed for incident " + incident.getReferenceNumber() + " (" + incident.getCategory() + "). Priority set to " + calculatedPriority + ". Draft work order created (#" + draftWo.getId() + ").";
            AgentToolLog t8 = logTool(savedRun, "generate_incident_summary",
                    "runId=" + savedRun.getId(),
                    summary);
            toolLogs.add(t8);

            // Update incident recommended priority
            incident.setRecommendedPriority(calculatedPriority);
            incidentRepository.save(incident);

            savedRun.setRecommendation(recommendation);
            savedRun.setExecutionStatus("COMPLETED");
            savedRun.setCompletedAt(LocalDateTime.now());
        }

        AgentRun finalRun = agentRunRepository.save(savedRun);

        auditService.logAction(officer, "AGENT_TRIAGE_RUN", "AgentRun", finalRun.getId(),
                "Incident: " + incident.getReferenceNumber() + ", Status: COMPLETED", null);

        return mapToDto(finalRun, toolLogs);
    }

    public AgentRunDto getAgentRun(Long id) {
        AgentRun run = agentRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent run not found: " + id));
        List<AgentToolLog> logs = toolLogRepository.findByAgentRunIdOrderByCreatedAtAsc(id);
        return mapToDto(run, logs);
    }

    public AgentRunDto getLatestAgentRunForIncident(Long incidentId) {
        AgentRun run = agentRunRepository.findTopByIncidentIdOrderByStartedAtDesc(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("No agent triage found for incident ID: " + incidentId));
        List<AgentToolLog> logs = toolLogRepository.findByAgentRunIdOrderByCreatedAtAsc(run.getId());
        return mapToDto(run, logs);
    }

    private AgentToolLog logTool(AgentRun run, String name, String in, String out) {
        AgentToolLog log = new AgentToolLog();
        log.setAgentRun(run);
        log.setToolName(name);
        log.setInputSummary(in);
        log.setOutputSummary(out);
        log.setExecutionStatus("SUCCESS");
        return toolLogRepository.save(log);
    }

    private String calculatePriority(Incident incident, double confidence, int duplicateCount) {
        String cat = incident.getCategory().toUpperCase();
        String desc = incident.getDescription().toLowerCase();

        if (cat.contains("STREETLIGHT") && (desc.contains("wire") || desc.contains("exposed") || desc.contains("spark"))) {
            return "CRITICAL";
        }
        if (cat.contains("WATER") && (desc.contains("burst") || desc.contains("gushing") || desc.contains("main"))) {
            return "CRITICAL";
        }
        if (cat.contains("POTHOLE") && (desc.contains("deep") || desc.contains("accident") || desc.contains("tire") || duplicateCount > 1)) {
            return "HIGH";
        }
        if (cat.contains("GARBAGE") && (desc.contains("biohazard") || desc.contains("hospital"))) {
            return "HIGH";
        }
        if (cat.contains("ROAD_CRACK") && desc.contains("bridge")) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    private String buildRecommendation(Incident incident, String priority, String citation, int dups) {
        return "### Automated Agent Triage Recommendation\n"
                + "- **Incident Reference**: " + incident.getReferenceNumber() + "\n"
                + "- **Damage Category**: " + incident.getCategory() + "\n"
                + "- **Assigned Department**: " + (incident.getAssignedDepartment() != null ? incident.getAssignedDepartment().getName() : "General Works") + "\n"
                + "- **Recommended Priority**: **" + priority + "**\n"
                + "- **Duplicate Proximity Check**: " + dups + " nearby incident(s) detected.\n"
                + "- **Knowledge Retrieval**: Complies with " + citation + ".\n\n"
                + "**Next Step**: A draft work order has been staged. Awaiting Municipal Officer review and formal approval.";
    }

    private AgentRunDto mapToDto(AgentRun run, List<AgentToolLog> logs) {
        AgentRunDto dto = new AgentRunDto();
        dto.setId(run.getId());
        dto.setIncidentId(run.getIncident().getId());
        dto.setExecutionStatus(run.getExecutionStatus());
        dto.setRecommendation(run.getRecommendation());
        dto.setStartedAt(run.getStartedAt());
        dto.setCompletedAt(run.getCompletedAt());

        List<AgentToolLogDto> logDtos = logs.stream().map(l -> {
            AgentToolLogDto ld = new AgentToolLogDto();
            ld.setId(l.getId());
            ld.setToolName(l.getToolName());
            ld.setInputSummary(l.getInputSummary());
            ld.setOutputSummary(l.getOutputSummary());
            ld.setExecutionStatus(l.getExecutionStatus());
            ld.setCreatedAt(l.getCreatedAt());
            return ld;
        }).collect(Collectors.toList());

        dto.setToolLogs(logDtos);
        return dto;
    }
}
