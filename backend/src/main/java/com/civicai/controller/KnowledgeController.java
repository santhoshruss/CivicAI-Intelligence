package com.civicai.controller;

import com.civicai.dto.ApiResponse;
import com.civicai.dto.DocumentResponseDto;
import com.civicai.dto.KnowledgeQueryDto;
import com.civicai.dto.KnowledgeResponseDto;
import com.civicai.entity.User;
import com.civicai.repository.UserRepository;
import com.civicai.security.CustomUserDetails;
import com.civicai.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final UserRepository userRepository;

    public KnowledgeController(KnowledgeService knowledgeService, UserRepository userRepository) {
        this.knowledgeService = knowledgeService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('MUNICIPAL_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "title", required = false) String title,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        DocumentResponseDto response = knowledgeService.uploadAndIndexDocument(file, category, title, user);
        return ResponseEntity.ok(ApiResponse.success("Maintenance document ingested and indexed in RAG FAISS vector store", response));
    }

    @PostMapping("/query")
    public ResponseEntity<ApiResponse<KnowledgeResponseDto>> queryKnowledge(
            @Valid @RequestBody KnowledgeQueryDto queryDto) {
        KnowledgeResponseDto response = knowledgeService.queryKnowledge(queryDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getDocuments() {
        List<DocumentResponseDto> response = knowledgeService.getAllDocuments();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
