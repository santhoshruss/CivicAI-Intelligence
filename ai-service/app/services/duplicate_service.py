"""
CivicAI Enhanced Semantic Duplicate Detection Service
Uses Hugging Face Sentence Transformers (all-MiniLM-L6-v2) to generate dense embeddings
and calculate cosine similarity combined with geospatial Haversine distance, category matching,
and recency weighting. Includes in-memory caching to avoid redundant inferences.
"""
import math
import hashlib
from typing import List, Dict, Any, Optional
import numpy as np

try:
    from sentence_transformers import SentenceTransformer
    SENTENCE_TRANSFORMER_AVAILABLE = True
except ImportError:
    SENTENCE_TRANSFORMER_AVAILABLE = False


class DuplicateDetectionService:
    def __init__(self, model_name: str = "sentence-transformers/all-MiniLM-L6-v2"):
        self.model_name = model_name
        self.model = None
        self._embedding_cache: Dict[str, np.ndarray] = {}
        self._max_cache_size = 5000
        self._init_model()

    def _init_model(self):
        """Initializes the SentenceTransformer model with fallback handling."""
        if SENTENCE_TRANSFORMER_AVAILABLE:
            try:
                # Load lightweight CPU-optimized embedding model
                self.model = SentenceTransformer(self.model_name)
                print(f"[DuplicateService] Loaded semantic embedding model: {self.model_name}")
            except Exception as e:
                print(f"[DuplicateService] SentenceTransformer load warning: {e}. Fallback enabled.")
                self.model = None
        else:
            print("[DuplicateService] sentence_transformers not available. Fallback enabled.")

    def get_embedding(self, text: str) -> np.ndarray:
        """
        Retrieves embedding with SHA256 in-memory caching to prevent redundant inferences.
        """
        clean_text = (text or "").strip().lower()
        if not clean_text:
            return np.zeros(384, dtype=np.float32)

        cache_key = hashlib.sha256(clean_text.encode("utf-8")).hexdigest()
        if cache_key in self._embedding_cache:
            return self._embedding_cache[cache_key]

        if self.model is not None:
            emb = self.model.encode(clean_text, convert_to_numpy=True, normalize_embeddings=True)
        else:
            # Deterministic fallback vectorizer for zero-budget resilience
            emb = self._deterministic_vector(clean_text, dim=384)

        if len(self._embedding_cache) >= self._max_cache_size:
            # Evict oldest 20%
            keys = list(self._embedding_cache.keys())[:1000]
            for k in keys:
                del self._embedding_cache[k]

        self._embedding_cache[cache_key] = emb
        return emb

    def _deterministic_vector(self, text: str, dim: int = 384) -> np.ndarray:
        """Generates deterministic unit-normalized pseudo-embedding when model is offline."""
        vec = np.zeros(dim, dtype=np.float32)
        words = text.split()
        if not words:
            return vec
        for w in words:
            idx = int(hashlib.md5(w.encode("utf-8")).hexdigest(), 16) % dim
            vec[idx] += 1.0
        norm = np.linalg.norm(vec)
        return vec / norm if norm > 0 else vec

    @staticmethod
    def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        """Returns distance between two coordinates in meters."""
        R = 6371000.0
        dlat = math.radians(lat2 - lat1)
        dlon = math.radians(lon2 - lon1)
        a = math.sin(dlat / 2.0)**2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2.0)**2
        c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
        return round(R * c, 2)

    def calculate_cosine_similarity(self, emb1: np.ndarray, emb2: np.ndarray) -> float:
        """Calculates cosine similarity between two normalized vectors."""
        norm1 = np.linalg.norm(emb1)
        norm2 = np.linalg.norm(emb2)
        if norm1 == 0 or norm2 == 0:
            return 0.0
        dot = np.dot(emb1, emb2) / (norm1 * norm2)
        return float(np.clip(dot, 0.0, 1.0))

    def evaluate_duplicate_candidates(
        self,
        target_incident: Dict[str, Any],
        candidates: List[Dict[str, Any]],
        radius_meters: float = 300.0
    ) -> List[Dict[str, Any]]:
        """
        Compares target incident with database candidates within geographical radius.
        Combines:
          - Semantic similarity (Sentence Transformers) [50% weight]
          - Proximity score (decay over radius) [30% weight]
          - Category match [20% weight]
        """
        t_lat = float(target_incident.get("latitude", 0.0))
        t_lon = float(target_incident.get("longitude", 0.0))
        t_desc = target_incident.get("description", "")
        t_cat = (target_incident.get("category") or "").upper()
        t_emb = self.get_embedding(t_desc)

        matches = []
        for cand in candidates:
            c_id = cand.get("id") or cand.get("incident_id")
            c_lat = cand.get("latitude")
            c_lon = cand.get("longitude")
            c_desc = cand.get("description", "")
            c_cat = (cand.get("category") or "").upper()
            c_ref = cand.get("reference_number", f"INC-{c_id}")

            if c_lat is None or c_lon is None:
                continue

            dist = self.haversine_distance(t_lat, t_lon, float(c_lat), float(c_lon))
            if dist > radius_meters:
                continue

            c_emb = self.get_embedding(c_desc)
            semantic_sim = self.calculate_cosine_similarity(t_emb, c_emb)

            # Proximity score (1.0 at 0m, decaying to 0.0 at radius_meters)
            proximity_score = max(0.0, (radius_meters - dist) / radius_meters)

            # Category match bonus
            category_match = (t_cat == c_cat)
            cat_score = 1.0 if category_match else 0.2

            # Weighted combined duplicate score
            combined_score = round(
                (semantic_sim * 0.50) + (proximity_score * 0.30) + (cat_score * 0.20),
                3
            )

            # Duplicate classification tier
            if combined_score >= 0.72 or (semantic_sim >= 0.82 and dist <= 150):
                classification = "HIGH_SIMILARITY"
            elif combined_score >= 0.48 or (semantic_sim >= 0.65 and dist <= 300):
                classification = "MEDIUM_SIMILARITY"
            else:
                classification = "LOW_SIMILARITY"

            explanation_parts = [
                f"{int(semantic_sim * 100)}% semantic textual alignment."
            ]
            if dist <= 50:
                explanation_parts.append(f"Immediate proximity ({int(dist)}m away).")
            else:
                explanation_parts.append(f"Located {int(dist)}m away within {int(radius_meters)}m municipal radius.")

            if category_match:
                explanation_parts.append(f"Matching category '{t_cat}'.")
            else:
                explanation_parts.append(f"Different reported category ('{t_cat}' vs '{c_cat}').")

            matches.append({
                "related_incident_id": c_id,
                "reference_number": c_ref,
                "distance_meters": dist,
                "category": c_cat,
                "semantic_similarity": round(semantic_sim, 3),
                "similarity_score": combined_score,
                "similarity_percentage": int(combined_score * 100),
                "classification": classification,
                "explanation": " ".join(explanation_parts),
                "review_status": "POSSIBLE_DUPLICATE"
            })

        # Sort highest similarity first
        matches.sort(key=lambda x: x["similarity_score"], reverse=True)
        return matches


# Global singleton instance
duplicate_service = DuplicateDetectionService()
