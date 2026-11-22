package com.civicai.util;

import com.civicai.exception.BadRequestException;
import com.civicai.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path evidenceStorageLocation;
    private final Path documentStorageLocation;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    public FileStorageService(@Value("${civicai.upload.dir:../uploads}") String uploadDir) {
        Path baseUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.evidenceStorageLocation = baseUploadPath.resolve("evidence").normalize();
        this.documentStorageLocation = baseUploadPath.resolve("documents").normalize();

        try {
            Files.createDirectories(this.evidenceStorageLocation);
            Files.createDirectories(this.documentStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directories.", ex);
        }
    }

    public String storeEvidenceFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPEG, PNG, and WebP images are permitted.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence.jpg");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.evidenceStorageLocation.resolve(storedFilename).normalize();
            if (!targetLocation.startsWith(this.evidenceStorageLocation)) {
                throw new BadRequestException("Invalid path traversal attempt.");
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store evidence file: " + ex.getMessage());
        }
    }

    public String storeDocumentFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "manual.pdf");
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF documents are allowed for maintenance SOP ingestion.");
        }

        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        try {
            Path targetLocation = this.documentStorageLocation.resolve(storedFilename).normalize();
            if (!targetLocation.startsWith(this.documentStorageLocation)) {
                throw new BadRequestException("Invalid path traversal attempt.");
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store document file: " + ex.getMessage());
        }
    }

    public Resource loadEvidenceAsResource(String filename) {
        try {
            Path filePath = this.evidenceStorageLocation.resolve(filename).normalize();
            if (!filePath.startsWith(this.evidenceStorageLocation)) {
                throw new BadRequestException("Invalid path traversal attempt.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Evidence file not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Evidence file not found: " + filename);
        }
    }

    public Path getEvidenceStorageLocation() {
        return evidenceStorageLocation;
    }

    public Path getDocumentStorageLocation() {
        return documentStorageLocation;
    }
}
