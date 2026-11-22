package com.civicai.controller;

import com.civicai.dto.*;
import com.civicai.entity.User;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.AnalysisService;
import com.civicai.service.DuplicateDetectionService;
import com.civicai.service.IncidentService;
import com.civicai.util.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final AnalysisService analysisService;
    private final DuplicateDetectionService duplicateService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public IncidentController(IncidentService incidentService,
                              AnalysisService analysisService,
                              DuplicateDetectionService duplicateService,
                              UserRepository userRepository,
                              FileStorageService fileStorageService) {
        this.incidentService = incidentService;
        this.analysisService = analysisService;
        this.duplicateService = duplicateService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponseDto>> createIncident(
            @Valid @RequestBody IncidentCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        IncidentResponseDto response = incidentService.createIncident(dto, user);
        return ResponseEntity.ok(ApiResponse.success("Incident reported successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentResponseDto>>> getAllIncidents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<IncidentResponseDto> response = incidentService.getAllIncidents(category, status, priority, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<IncidentResponseDto>>> getMyIncidents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<IncidentResponseDto> response = incidentService.getMyIncidents(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponseDto>> getIncidentById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        IncidentResponseDto response = incidentService.getIncidentById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<IncidentResponseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        IncidentResponseDto response = incidentService.updateStatus(id, dto, user);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", response));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<IncidentResponseDto>> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        IncidentResponseDto response = incidentService.uploadEvidence(id, file, user);
        return ResponseEntity.ok(ApiResponse.success("Evidence photo uploaded successfully", response));
    }

    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnalysisResponseDto>> triggerAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        AnalysisResponseDto response = analysisService.analyzeIncident(id, user);
        return ResponseEntity.ok(ApiResponse.success("AI Damage Analysis completed", response));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<ApiResponse<AnalysisResponseDto>> getAnalysis(@PathVariable Long id) {
        AnalysisResponseDto response = analysisService.getLatestAnalysis(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/duplicates")
    public ResponseEntity<ApiResponse<List<DuplicateResponseDto>>> getDuplicates(@PathVariable Long id) {
        List<DuplicateResponseDto> response = duplicateService.findAndRecordDuplicates(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/duplicates/confirm")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DuplicateResponseDto>> confirmDuplicate(
            @PathVariable Long id,
            @Valid @RequestBody DuplicateConfirmDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        DuplicateResponseDto response = duplicateService.confirmDuplicate(dto, user);
        return ResponseEntity.ok(ApiResponse.success("Duplicate review saved", response));
    }

    @GetMapping("/evidence/file/{filename}")
    public ResponseEntity<Resource> serveEvidenceFile(@PathVariable String filename) {
        Resource file = fileStorageService.loadEvidenceAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }
}
