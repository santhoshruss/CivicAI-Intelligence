package com.civicai.dto;

import java.util.List;

public class KnowledgeResponseDto {
    private String answer;
    private List<SourceCitation> sources;
    private boolean fallbackUsed;

    public KnowledgeResponseDto() {}

    public KnowledgeResponseDto(String answer, List<SourceCitation> sources, boolean fallbackUsed) {
        this.answer = answer;
        this.sources = sources;
        this.fallbackUsed = fallbackUsed;
    }

    public static class SourceCitation {
        private String documentName;
        private int pageNumber;
        private String excerpt;
        private double score;

        public SourceCitation() {}

        public SourceCitation(String documentName, int pageNumber, String excerpt, double score) {
            this.documentName = documentName;
            this.pageNumber = pageNumber;
            this.excerpt = excerpt;
            this.score = score;
        }

        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public String getExcerpt() { return excerpt; }
        public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<SourceCitation> getSources() { return sources; }
    public void setSources(List<SourceCitation> sources) { this.sources = sources; }

    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}
