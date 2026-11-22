package com.civicai.dto;

import java.util.List;

public class ChatResponseDto {
    private String reply;
    private String suggestedAction;
    private String actionUrl;
    private List<KnowledgeResponseDto.SourceCitation> sources;

    public ChatResponseDto() {}

    public ChatResponseDto(String reply) {
        this.reply = reply;
    }

    public ChatResponseDto(String reply, String suggestedAction, String actionUrl, List<KnowledgeResponseDto.SourceCitation> sources) {
        this.reply = reply;
        this.suggestedAction = suggestedAction;
        this.actionUrl = actionUrl;
        this.sources = sources;
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public List<KnowledgeResponseDto.SourceCitation> getSources() { return sources; }
    public void setSources(List<KnowledgeResponseDto.SourceCitation> sources) { this.sources = sources; }
}
