package com.civicai.service;

import com.civicai.dto.DuplicateConfirmDto;
import com.civicai.dto.DuplicateResponseDto;
import com.civicai.entity.DuplicateIncident;
import com.civicai.entity.Incident;
import com.civicai.entity.User;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.repository.DuplicateIncidentRepository;
import com.civicai.repository.IncidentRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetectionService.class);

    private final IncidentRepository incidentRepository;
    private final DuplicateIncidentRepository duplicateIncidentRepository;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    @Value("${civicai.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public DuplicateDetectionService(IncidentRepository incidentRepository,
                                     DuplicateIncidentRepository duplicateIncidentRepository,
                                     AuditService auditService) {
        this.incidentRepository = incidentRepository;
        this.duplicateIncidentRepository = duplicateIncidentRepository;
        this.auditService = auditService;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public List<DuplicateResponseDto> findAndRecordDuplicates(Long incidentId) {
        Incident target = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        double lat = target.getLatitude().doubleValue();
        double lon = target.getLongitude().doubleValue();
        double radiusMeters = 300.0;

        List<Incident> nearby = incidentRepository.findNearbyIncidents(target.getId(), lat, lon, radiusMeters);
        if (nearby.isEmpty()) {
            return Collections.emptyList();
        }

        boolean aiServiceSuccess = false;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put("id", target.getId());
            targetMap.put("latitude", lat);
            targetMap.put("longitude", lon);
            targetMap.put("category", target.getCategory());
            targetMap.put("description", target.getDescription());

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

            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("target", targetMap);
            requestPayload.put("candidates", candidateList);
            requestPayload.put("radius_meters", radiusMeters);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    aiServiceUrl + "/api/ai/detect-duplicates",
                    entity,
                    Map.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                List<Map<String, Object>> matches = (List<Map<String, Object>>) resp.getBody().get("matches");
                if (matches != null) {
                    for (Map<String, Object> match : matches) {
                        Long relatedId = Long.valueOf(match.get("related_incident_id").toString());
                        if (!duplicateIncidentRepository.existsByIncidentIdAndRelatedIncidentId(target.getId(), relatedId)) {
                            Incident candidate = incidentRepository.findById(relatedId).orElse(null);
                            if (candidate != null) {
                                DuplicateIncident duplicate = new DuplicateIncident();
                                duplicate.setIncident(target);
                                duplicate.setRelatedIncident(candidate);
                                duplicate.setDistanceMeters(Double.valueOf(match.get("distance_meters").toString()));
                                duplicate.setSimilarityScore(Double.valueOf(match.get("similarity_score").toString()));
                                duplicate.setSimilarityLevel(match.getOrDefault("classification", "MEDIUM_SIMILARITY").toString());
                                duplicate.setExplanation(match.getOrDefault("explanation", "").toString());
                                duplicate.setReviewStatus("POSSIBLE_DUPLICATE");
                                duplicateIncidentRepository.save(duplicate);
                            }
                        }
                    }
                    aiServiceSuccess = true;
                }
            }
        } catch (Exception ex) {
            log.warn("Hugging Face semantic duplicate detection call failed: {}. Using local fallback.", ex.getMessage());
        }

        if (!aiServiceSuccess) {
            // Local fallback
            for (Incident candidate : nearby) {
                if (!duplicateIncidentRepository.existsByIncidentIdAndRelatedIncidentId(target.getId(), candidate.getId())) {
                    double distance = calculateHaversineDistance(
                            lat, lon,
                            candidate.getLatitude().doubleValue(),
                            candidate.getLongitude().doubleValue()
                    );
                    double textSim = calculateTextSimilarity(target.getDescription(), candidate.getDescription());
                    boolean categoryMatch = target.getCategory().equalsIgnoreCase(candidate.getCategory());
                    double score = (categoryMatch ? 0.5 : 0.1) + (textSim * 0.3) + (Math.max(0, (radiusMeters - distance) / radiusMeters) * 0.2);

                    String level = score >= 0.75 ? "HIGH_SIMILARITY" : (score >= 0.5 ? "MEDIUM_SIMILARITY" : "LOW_SIMILARITY");

                    DuplicateIncident duplicate = new DuplicateIncident();
                    duplicate.setIncident(target);
                    duplicate.setRelatedIncident(candidate);
                    duplicate.setDistanceMeters(Math.round(distance * 100.0) / 100.0);
                    duplicate.setSimilarityScore(Math.round(score * 100.0) / 100.0);
                    duplicate.setSimilarityLevel(level);
                    duplicate.setExplanation(String.format("Local proximity match: %dm away. Category match: %s.", (int) distance, categoryMatch));
                    duplicate.setReviewStatus("POSSIBLE_DUPLICATE");
                    duplicateIncidentRepository.save(duplicate);
                }
            }
        }

        return duplicateIncidentRepository.findByIncidentId(target.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DuplicateResponseDto> getDuplicatesForIncident(Long incidentId) {
        return duplicateIncidentRepository.findByIncidentId(incidentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DuplicateResponseDto confirmDuplicate(DuplicateConfirmDto dto, User officer) {
        DuplicateIncident duplicate = duplicateIncidentRepository.findById(dto.getDuplicateId())
                .orElseThrow(() -> new ResourceNotFoundException("Duplicate record not found: " + dto.getDuplicateId()));

        duplicate.setReviewStatus(dto.getAction().toUpperCase());
        DuplicateIncident saved = duplicateIncidentRepository.save(duplicate);

        auditService.logAction(officer, "DUPLICATE_REVIEWED", "DuplicateIncident", saved.getId(),
                "Status set to: " + saved.getReviewStatus(), null);

        return mapToDto(saved);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double calculateTextSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String[] words1 = s1.toLowerCase().split("\\W+");
        String[] words2 = s2.toLowerCase().split("\\W+");

        int common = 0;
        for (String w1 : words1) {
            for (String w2 : words2) {
                if (w1.equals(w2) && w1.length() > 3) {
                    common++;
                    break;
                }
            }
        }
        return words1.length > 0 ? (double) common / words1.length : 0.0;
    }

    private DuplicateResponseDto mapToDto(DuplicateIncident duplicate) {
        DuplicateResponseDto dto = new DuplicateResponseDto();
        dto.setId(duplicate.getId());
        dto.setIncidentId(duplicate.getIncident().getId());
        dto.setRelatedIncidentId(duplicate.getRelatedIncident().getId());
        dto.setRelatedReferenceNumber(duplicate.getRelatedIncident().getReferenceNumber());
        dto.setRelatedCategory(duplicate.getRelatedIncident().getCategory());
        dto.setRelatedAddress(duplicate.getRelatedIncident().getAddress());
        dto.setDistanceMeters(duplicate.getDistanceMeters());
        dto.setSimilarityScore(duplicate.getSimilarityScore());
        dto.setSimilarityLevel(duplicate.getSimilarityLevel());
        dto.setExplanation(duplicate.getExplanation());
        dto.setReviewStatus(duplicate.getReviewStatus());
        dto.setCreatedAt(duplicate.getCreatedAt());
        return dto;
    }
}
