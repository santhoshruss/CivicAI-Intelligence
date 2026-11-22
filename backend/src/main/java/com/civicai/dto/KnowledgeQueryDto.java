package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;

public class KnowledgeQueryDto {
    @NotBlank(message = "Query question is required")
    private String query;
    private String category;

    public KnowledgeQueryDto() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
