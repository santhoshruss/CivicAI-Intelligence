package com.civicai.dto;

import java.time.LocalDateTime;

public class DocumentResponseDto {
    private Long id;
    private String title;
    private String category;
    private String originalFilename;
    private Long fileSize;
    private Integer chunkCount;
    private String indexedStatus;
    private LocalDateTime createdAt;

    public DocumentResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public String getIndexedStatus() { return indexedStatus; }
    public void setIndexedStatus(String indexedStatus) { this.indexedStatus = indexedStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
