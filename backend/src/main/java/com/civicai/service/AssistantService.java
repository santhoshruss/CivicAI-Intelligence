package com.civicai.service;

import com.civicai.dto.ChatRequestDto;
import com.civicai.dto.ChatResponseDto;
import com.civicai.dto.KnowledgeQueryDto;
import com.civicai.dto.KnowledgeResponseDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AssistantService {

    private final KnowledgeService knowledgeService;

    public AssistantService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public ChatResponseDto processMessage(ChatRequestDto request, String userRole) {
        String msg = request.getMessage().trim().toLowerCase();

        // 1. Reporting Damage guidance
        if (msg.contains("how to report") || msg.contains("report damage") || msg.contains("submit issue") || msg.contains("file report")) {
            return new ChatResponseDto(
                    "To report infrastructure damage, navigate to the **Report Damage** page. "
                    + "Select the hazard category (Pothole, Road Crack, Streetlight, Water Leak, or Garbage), "
                    + "click 'Use Current GPS Location' to capture precise coordinates, and attach a clear photo of the damage.",
                    "Go to Report Form",
                    "report-incident.html",
                    null
            );
        }

        // 2. Status Tracking queries
        if (msg.contains("status") && (msg.contains("my report") || msg.contains("track") || msg.contains("incident"))) {
            return new ChatResponseDto(
                    "You can track the live status of all your submitted reports on your **Citizen Dashboard**. "
                    + "Statuses progress through: **REPORTED** ➔ **UNDER_REVIEW** ➔ **WORK_ORDER_CREATED** ➔ **IN_PROGRESS** ➔ **RESOLVED**.",
                    "View My Dashboard",
                    "citizen-dashboard.html",
                    null
            );
        }

        // 3. Priority levels explanation
        if (msg.contains("priority") || msg.contains("critical") || msg.contains("severity")) {
            return new ChatResponseDto(
                    "CivicAI calculates incident priority based on public safety risk:\n"
                    + "- **CRITICAL**: Immediate danger to human life (e.g., exposed 240V/480V wires, active water main bursting).\n"
                    + "- **HIGH**: Arterial road cavities (>30cm), high traffic hazard, or multiple duplicates.\n"
                    + "- **MEDIUM**: Standard asphalt cracks, minor leaks, non-arterial settlement.\n"
                    + "- **LOW**: Overflowing trash bins, aesthetic wear.",
                    "View Incident Backlog",
                    "incidents.html",
                    null
            );
        }

        // 4. Map & Visualisation
        if (msg.contains("map") || msg.contains("where") || msg.contains("location")) {
            return new ChatResponseDto(
                    "The **City Map** plots all active and resolved infrastructure incidents in real time using Leaflet.js and OpenStreetMap. Markers are color-coded by priority (🔴 Critical, 🟠 High, 🟡 Medium, 🟢 Low).",
                    "Open City Map",
                    "map.html",
                    null
            );
        }

        // 5. Work Orders & Maintenance Operations
        if (msg.contains("work order") || msg.contains("dispatch") || msg.contains("inspector")) {
            return new ChatResponseDto(
                    "Work orders are staged automatically by our Agentic AI workflow in **DRAFT** status. "
                    + "A Municipal Officer must review and approve them before they are assigned to certified Field Inspectors for on-site repair and verification.",
                    "View Work Orders",
                    "work-orders.html",
                    null
            );
        }

        // 6. Technical Engineering & SOP inquiries -> Route to FAISS RAG Knowledge Base!
        KnowledgeQueryDto ragQuery = new KnowledgeQueryDto();
        ragQuery.setQuery(request.getMessage());
        KnowledgeResponseDto ragResponse = knowledgeService.queryKnowledge(ragQuery);

        if (ragResponse != null && ragResponse.getSources() != null && !ragResponse.getSources().isEmpty()) {
            return new ChatResponseDto(
                    ragResponse.getAnswer(),
                    "Explore RAG Guidelines",
                    "rag-assistant.html",
                    ragResponse.getSources()
            );
        }

        // 7. General Fallback with helpful capabilities
        return new ChatResponseDto(
                "I am your CivicAI Intelligent Assistant. I can assist you with:\n"
                + "• Reporting new public hazards (potholes, streetlights, water main leaks)\n"
                + "• Retrieving technical repair procedures from official city SOP manuals\n"
                + "• Explaining priority calculation & work order resolution\n"
                + "• Navigating the live City Map and analytics.",
                "Report an Issue",
                "report-incident.html",
                new ArrayList<>()
        );
    }
}
