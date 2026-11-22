"""
CivicAI Multilingual Citizen Complaint NLP Service
Supports English, Tamil (தமிழ் script), and Tanglish (Tamil in Latin/English script).
Performs language detection, Tanglish normalization to English, keyword extraction,
and category/urgency intent classification.
"""
import re
from typing import Dict, Any, List

class MultilingualComplaintService:

    # Tamil Unicode block: \u0B80-\u0BFF
    TAMIL_REGEX = re.compile(r"[\u0B80-\u0BFF]")

    # Tanglish dictionary for infrastructure terminology
    TANGLISH_MAP = {
        # Location & Pronouns
        r"\benga\b": "our",
        r"\bnamma\b": "our",
        r"\btheru\b|\btherula\b": "street",
        r"\bstreet\s*la\b": "on the street",
        r"\broad\s*la\b": "on the road",
        r"\barea\s*la\b": "in the area",
        r"\bkitta\b|\bpakkathula\b": "near",

        # Road & Potholes
        r"\bpallam\b|\bgundu\s*kuzhi\b": "pothole",
        r"\bperiya\s*pallam\b": "large pothole",
        r"\broatu\b": "road",
        r"\bodanjurukku\b|\bodanju\s*poirukku\b": "broken and damaged",
        r"\bvedippu\b": "crack",

        # Descriptors & Severity
        r"\bromba\s*periya\b": "very large",
        r"\bperiya\b": "big",
        r"\bromba\b": "very",
        r"\birukku\b|\biruku\b": "is present",
        r"\baabathu\b|\bbayama\s*irukku\b": "dangerous hazard",
        r"\baccident\s*aagudhu\b": "causing accidents",
        r"\bseekiram\b|\budane\b": "urgently",

        # Streetlights & Electrical
        r"\bvilakku\b": "light",
        r"\bstreetlight\b": "streetlight",
        r"\beriyala\b|\beriya\s*maattengudhu\b": "is not working",
        r"\bvelicham\s*illa\b|\birutta\s*irukku\b": "very dark no light",
        r"\bcurrent\s*kambu\b": "electric pole",
        r"\bwire\s*arunju\b": "wire snapped and hanging",
        r"\bspark\s*varudhu\b": "sparks emitting",

        # Water & Sewage
        r"\bthanneer\b|\bthanni\b": "water",
        r"\bkuzhai\b": "pipe",
        r"\bthanneer\s*odudhu\b": "water is gushing",
        r"\bodanja\s*pipe\b": "broken pipe",
        r"\bvellam\b|\bthanni\s*thekki\b": "waterlogging flooding",
        r"\bdrainage\s*odudhu\b": "sewage overflowing",

        # Garbage & Sanitation
        r"\bkuppai\b|\bkuppa\b": "garbage waste",
        r"\bkuppai\s*thotti\b": "garbage bin",
        r"\ballala\b|\bedukkala\b": "not cleared for days",
        r"\bnaatram\b|\bsmell\s*adikkuthu\b": "terrible rotting odor",
        r"\bkozhundhu\b|\bkeeda\b": "insects and rodents"
    }

    # Tamil script dictionary
    TAMIL_SCRIPT_MAP = {
        "பள்ளம்": "pothole",
        "குண்டு குழி": "deep pothole",
        "ரோடு": "road",
        "உடைந்து": "broken",
        "விளக்கு": "streetlight",
        "எரியவில்லை": "not working",
        "தண்ணீர்": "water",
        "கழிவுநீர்": "sewage",
        "குப்பை": "garbage",
        "நாற்றம்": "bad smell",
        "ஆபத்து": "danger"
    }

    CATEGORY_KEYWORDS = {
        "POTHOLE": ["pothole", "cavity", "crater", "pallam", "asphalt depression", "road pit"],
        "ROAD_CRACK": ["crack", "fissure", "vedippu", "buckling", "surface tear", "broken road"],
        "STREETLIGHT_DAMAGE": ["light", "streetlight", "lamp", "vilakku", "pole", "wire", "spark", "dark", "eriyala"],
        "WATER_LEAKAGE": ["water", "leak", "pipe", "thanneer", "thanni", "gushing", "drainage", "burst", "flood"],
        "GARBAGE_OVERFLOW": ["garbage", "trash", "waste", "kuppai", "dump", "bin", "odor", "smell", "debris"]
    }

    DEPARTMENT_MAP = {
        "POTHOLE": "Road Maintenance",
        "ROAD_CRACK": "Road Maintenance",
        "STREETLIGHT_DAMAGE": "Electrical & Lighting Department",
        "WATER_LEAKAGE": "Water Supply & Sewage Board",
        "GARBAGE_OVERFLOW": "Sanitation & Waste Management"
    }

    def detect_language(self, text: str) -> str:
        """Determines if the text is Tamil script, Tanglish, or English."""
        if not text or not text.strip():
            return "English"

        if self.TAMIL_REGEX.search(text):
            return "Tamil"

        # Check for characteristic Tanglish words
        lower = text.lower()
        tanglish_tokens = [
            "enga", "namma", "theru", "pallam", "irukku", "odanjurukku",
            "kuppai", "thanneer", "thanni", "eriyala", "kitta", "romba",
            "periya", "aabathu", "allala", "kuzhai", "vedippu"
        ]
        if any(re.search(r"\b" + re.escape(w) + r"\b", lower) for w in tanglish_tokens):
            return "Tanglish"

        return "English"

    def normalize_complaint(self, text: str) -> str:
        """Converts Tanglish and Tamil terms into a clear normalized English representation."""
        if not text:
            return ""

        result = text

        # Translate Tamil script keywords
        for t_word, e_word in self.TAMIL_SCRIPT_MAP.items():
            result = result.replace(t_word, e_word)

        # Apply regex Tanglish normalization
        for pattern, replacement in self.TANGLISH_MAP.items():
            result = re.sub(pattern, replacement, result, flags=re.IGNORECASE)

        # Cleanup whitespace
        result = re.sub(r"\s+", " ", result).strip()
        return result

    def classify_complaint(self, text: str) -> Dict[str, Any]:
        """
        Processes complaint text, returning detected language, normalized description,
        predicted category, urgency, and responsible department.
        """
        raw_text = text or ""
        lang = self.detect_language(raw_text)
        normalized = self.normalize_complaint(raw_text)

        search_corpus = f"{raw_text.lower()} {normalized.lower()}"

        # Category matching
        scores = {}
        for cat, kws in self.CATEGORY_KEYWORDS.items():
            score = sum(1 for kw in kws if kw in search_corpus)
            scores[cat] = score

        best_category = max(scores, key=scores.get) if any(scores.values()) else "POTHOLE"

        # Urgency assessment
        urgency = "MEDIUM"
        critical_kws = ["wire", "spark", "shock", "burst", "gushing", "accident", "imminent", "aabathu", "emergency"]
        high_kws = ["deep", "severe", "hazard", "large", "overflowing", "rotting", "periya", "urgent", "danger"]

        if any(w in search_corpus for w in critical_kws):
            urgency = "CRITICAL"
        elif any(w in search_corpus for w in high_kws):
            urgency = "HIGH"

        # Extract highlighted keywords
        extracted_keywords = []
        for kw_list in self.CATEGORY_KEYWORDS.values():
            for kw in kw_list:
                if kw in search_corpus and kw not in extracted_keywords:
                    extracted_keywords.append(kw)

        return {
            "detected_language": lang,
            "original_text": raw_text,
            "normalized_english": normalized if lang != "English" else raw_text,
            "predicted_category": best_category,
            "urgency": urgency,
            "suggested_department": self.DEPARTMENT_MAP.get(best_category, "Road Maintenance"),
            "extracted_keywords": extracted_keywords[:6],
            "confidence": 0.88 if any(scores.values()) else 0.70
        }


# Global singleton instance
nlp_service = MultilingualComplaintService()
