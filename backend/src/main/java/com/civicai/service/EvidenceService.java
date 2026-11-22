package com.civicai.service;

import com.civicai.entity.Evidence;
import com.civicai.entity.Incident;
import com.civicai.entity.User;
import com.civicai.exception.ResourceNotFoundException;
import com.civicai.repository.EvidenceRepository;
import com.civicai.util.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final FileStorageService fileStorageService;

    public EvidenceService(EvidenceRepository evidenceRepository, FileStorageService fileStorageService) {
        this.evidenceRepository = evidenceRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Evidence saveEvidence(Incident incident, MultipartFile file, User user) {
        String storedFilename = fileStorageService.storeEvidenceFile(file);

        Evidence evidence = new Evidence();
        evidence.setIncident(incident);
        evidence.setStoredFilename(storedFilename);
        evidence.setOriginalFilename(file.getOriginalFilename());
        evidence.setContentType(file.getContentType());
        evidence.setFileSize(file.getSize());
        evidence.setUploadedBy(user);

        return evidenceRepository.save(evidence);
    }

    public List<Evidence> getEvidenceForIncident(Long incidentId) {
        return evidenceRepository.findByIncidentId(incidentId);
    }
}
