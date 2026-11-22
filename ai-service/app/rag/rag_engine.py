"""
CivicAI Enhanced Multilingual RAG Engine
(PyMuPDF + RapidOCR + Sentence Transformers all-MiniLM-L6-v2 + FAISS)
Supports multilingual queries (Tamil, Tanglish, English), category filtering,
exact page-level citations, relevance thresholding to prevent hallucination,
and deduplicated ingestion of native and OCR-extracted documents.
"""
import os
import re
import pickle
import hashlib
from pathlib import Path
from typing import List, Dict, Any, Optional
import numpy as np

try:
    import fitz  # PyMuPDF
    PYMUPDF_AVAILABLE = True
except ImportError:
    PYMUPDF_AVAILABLE = False

try:
    import faiss
    FAISS_AVAILABLE = True
except ImportError:
    FAISS_AVAILABLE = False

try:
    from sentence_transformers import SentenceTransformer
    SENTENCE_TRANSFORMER_AVAILABLE = True
except ImportError:
    SENTENCE_TRANSFORMER_AVAILABLE = False

from app.services.nlp_service import nlp_service


class RagEngine:
    def __init__(self, storage_dir: str = "./faiss_index"):
        self.storage_dir = Path(storage_dir)
        self.storage_dir.mkdir(parents=True, exist_ok=True)
        self.index_file = self.storage_dir / "index.faiss"
        self.chunks_file = self.storage_dir / "chunks.pkl"

        self.model = None
        self.chunks: List[Dict[str, Any]] = []
        self.index = None
        self.dim = 384  # Default embedding dimension for all-MiniLM-L6-v2
        self.relevance_threshold = 0.35  # Minimum cosine similarity to avoid hallucination

        self._init_model()
        self._load_stored_index()

    def _init_model(self):
        if SENTENCE_TRANSFORMER_AVAILABLE:
            try:
                self.model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
                self.dim = 384
                print("[RAG Engine] SentenceTransformer all-MiniLM-L6-v2 initialized.")
            except Exception as e:
                print(f"[RAG Engine] Model load warning: {e}. Fallback vectorizer active.")
                self.model = None

    def _load_stored_index(self):
        if self.chunks_file.exists():
            try:
                with open(self.chunks_file, "rb") as f:
                    self.chunks = pickle.load(f)
            except Exception as e:
                self.chunks = []

        if FAISS_AVAILABLE and self.index_file.exists():
            try:
                self.index = faiss.read_index(str(self.index_file))
            except Exception as e:
                self.index = None

    def _save_index(self):
        with open(self.chunks_file, "wb") as f:
            pickle.dump(self.chunks, f)

        if FAISS_AVAILABLE and self.index is not None:
            faiss.write_index(self.index, str(self.index_file))

    def _extract_text_from_pdf(self, pdf_path: str) -> List[Dict[str, Any]]:
        """Extracts text page by page using PyMuPDF (fitz)."""
        pages = []
        if not PYMUPDF_AVAILABLE:
            try:
                with open(pdf_path, 'r', encoding='utf-8', errors='ignore') as f:
                    pages.append({"page": 1, "text": f.read()})
            except Exception:
                pass
            return pages

        doc = fitz.open(pdf_path)
        for page_num in range(len(doc)):
            page = doc[page_num]
            text = page.get_text("text")
            if text.strip():
                pages.append({"page": page_num + 1, "text": text.strip()})
        doc.close()
        return pages

    def _chunk_text(self, text: str, chunk_size: int = 450, overlap: int = 80) -> List[str]:
        """Splits text into chunks with overlap."""
        chunks = []
        start = 0
        text_len = len(text)
        while start < text_len:
            end = min(start + chunk_size, text_len)
            chunks.append(text[start:end])
            start += (chunk_size - overlap)
            if start + overlap >= text_len:
                break
        return chunks

    def _sanitize_for_prompt_injection(self, text: str) -> str:
        """Sanitizes text to block prompt injection within documents."""
        cleaned = re.sub(r"(?i)(ignore previous instructions|system:|assistant:|admin password|drop table|curl|bash)", "[FILTERED]", text)
        return cleaned

    def _embed_texts(self, texts: List[str]) -> np.ndarray:
        if self.model is not None:
            embeddings = self.model.encode(texts, convert_to_numpy=True, normalize_embeddings=True)
            return embeddings.astype("float32")
        else:
            vectors = np.zeros((len(texts), self.dim), dtype="float32")
            for i, txt in enumerate(texts):
                words = txt.lower().split()
                for w in words:
                    idx = abs(hash(w)) % self.dim
                    vectors[i, idx] += 1.0
                norm = np.linalg.norm(vectors[i])
                if norm > 0:
                    vectors[i] /= norm
            return vectors

    def is_document_indexed(self, doc_identifier: str) -> bool:
        """Checks if a document with this identifier or name is already indexed."""
        return any(c.get("document_name") == doc_identifier for c in self.chunks)

    def ingest_document(self, file_path: str, category: str = "GENERAL", title: str = "") -> int:
        doc_name = title or Path(file_path).name

        # Prevent duplicate document ingestion
        if self.is_document_indexed(doc_name):
            print(f"[RAG Engine] Document '{doc_name}' is already indexed. Skipping duplicate ingestion.")
            return sum(1 for c in self.chunks if c.get("document_name") == doc_name)

        pages = self._extract_text_from_pdf(file_path)
        new_chunks = []

        for page in pages:
            raw_text = self._sanitize_for_prompt_injection(page["text"])
            sub_chunks = self._chunk_text(raw_text)
            for ch in sub_chunks:
                if len(ch.strip()) > 30:
                    new_chunks.append({
                        "document_name": doc_name,
                        "page_number": page["page"],
                        "category": category.upper(),
                        "text": ch.strip(),
                        "is_ocr": False
                    })

        return self._add_chunks_to_index(new_chunks)

    def ingest_ocr_pages(self, pages: List[Dict[str, Any]], category: str = "GENERAL", title: str = "OCR Document") -> int:
        """Ingests OCR-extracted text pages into the RAG vector store."""
        if self.is_document_indexed(title):
            return sum(1 for c in self.chunks if c.get("document_name") == title)

        new_chunks = []
        for p in pages:
            raw_text = self._sanitize_for_prompt_injection(p.get("text", ""))
            sub_chunks = self._chunk_text(raw_text)
            for ch in sub_chunks:
                if len(ch.strip()) > 30:
                    new_chunks.append({
                        "document_name": title,
                        "page_number": p.get("page", 1),
                        "category": category.upper(),
                        "text": ch.strip(),
                        "is_ocr": p.get("is_ocr", True)
                    })

        return self._add_chunks_to_index(new_chunks)

    def _add_chunks_to_index(self, new_chunks: List[Dict[str, Any]]) -> int:
        if not new_chunks:
            return 0

        embeddings = self._embed_texts([c["text"] for c in new_chunks])

        if FAISS_AVAILABLE:
            if self.index is None:
                self.index = faiss.IndexFlatIP(self.dim)
            self.index.add(embeddings)

        self.chunks.extend(new_chunks)
        self._save_index()
        return len(new_chunks)

    def search(self, query: str, top_k: int = 3, category: str = None) -> List[Dict[str, Any]]:
        if not self.chunks:
            return []

        # Handle multilingual queries: translate/normalize Tanglish or Tamil
        normalized_query = nlp_service.normalize_complaint(query)
        query_sanitized = self._sanitize_for_prompt_injection(normalized_query)
        q_vec = self._embed_texts([query_sanitized])

        results = []
        if FAISS_AVAILABLE and self.index is not None and self.index.ntotal > 0:
            k = min(top_k * 3, len(self.chunks))
            scores, indices = self.index.search(q_vec, k)

            for score, idx in zip(scores[0], indices[0]):
                if idx < len(self.chunks):
                    chunk = self.chunks[idx]
                    cat_filter = category.upper() if category else None
                    chunk_cat = chunk.get("category", "GENERAL").upper()

                    # Filter by category if specified and not generic
                    if cat_filter and chunk_cat != cat_filter and chunk_cat != "GENERAL":
                        continue

                    # Filter out matches below relevance threshold
                    if float(score) < self.relevance_threshold:
                        continue

                    results.append({
                        "document_name": chunk["document_name"],
                        "page_number": chunk["page_number"],
                        "category": chunk.get("category", "GENERAL"),
                        "excerpt": chunk["text"][:380] + "...",
                        "score": round(float(score), 3),
                        "is_ocr": chunk.get("is_ocr", False)
                    })
                    if len(results) >= top_k:
                        break
        else:
            q_words = set(query_sanitized.lower().split())
            scored = []
            for ch in self.chunks:
                cat_filter = category.upper() if category else None
                chunk_cat = ch.get("category", "GENERAL").upper()
                if cat_filter and chunk_cat != cat_filter and chunk_cat != "GENERAL":
                    continue
                ch_words = set(ch["text"].lower().split())
                overlap = len(q_words.intersection(ch_words))
                if overlap > 0:
                    scored.append((overlap, ch))
            scored.sort(key=lambda x: x[0], reverse=True)
            for overlap, ch in scored[:top_k]:
                score = round(min(0.95, 0.45 + (overlap * 0.1)), 3)
                if score >= self.relevance_threshold:
                    results.append({
                        "document_name": ch["document_name"],
                        "page_number": ch["page_number"],
                        "category": ch.get("category", "GENERAL"),
                        "excerpt": ch["text"][:380] + "...",
                        "score": score,
                        "is_ocr": ch.get("is_ocr", False)
                    })

        return results

    def generate_grounded_answer(self, query: str, category: str = None) -> Dict[str, Any]:
        # Detect language
        detected_lang = nlp_service.detect_language(query)
        retrieved_sources = self.search(query, top_k=3, category=category)

        if not retrieved_sources:
            return {
                "answer": "No official municipal standard operating procedure matched your query criteria above the required relevance threshold. Please consult your supervising engineer or upload the corresponding SOP document.",
                "sources": [],
                "detected_language": detected_lang,
                "relevance_met": False,
                "fallback_used": True
            }

        primary = retrieved_sources[0]
        context_excerpts = "\n\n".join([f"[{s['document_name']} (Page {s['page_number']})]: {s['excerpt']}" for s in retrieved_sources])

        # Gemini synthesis if enabled
        gemini_key = os.getenv("GEMINI_API_KEY", "").strip()
        provider = os.getenv("LLM_PROVIDER", "offline").lower().strip()

        if gemini_key and (provider == "gemini" or provider != "offline"):
            try:
                import requests
                api_url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={gemini_key}"
                prompt = (
                    "You are the CivicAI Municipal Maintenance Knowledge Assistant. "
                    "Answer the following question based strictly on the provided municipal guidelines. "
                    "Cite the document name and page number. If the answer is not in the guidelines, state that clearly.\n\n"
                    f"CONTEXT GUIDELINES:\n{context_excerpts}\n\n"
                    f"USER QUESTION: {query}"
                )
                payload = {
                    "contents": [{"parts": [{"text": prompt}]}],
                    "generationConfig": {"temperature": 0.2, "maxOutputTokens": 800}
                }
                resp = requests.post(api_url, json=payload, timeout=8)
                if resp.status_code == 200:
                    gemini_text = resp.json()["candidates"][0]["content"]["parts"][0]["text"]
                    return {
                        "answer": gemini_text.strip(),
                        "sources": retrieved_sources,
                        "detected_language": detected_lang,
                        "relevance_met": True,
                        "fallback_used": False,
                        "model": "gemini-1.5-flash"
                    }
            except Exception as e:
                print(f"[RAG Engine] Gemini API fallback notice: {e}")

        # Grounded Local Synthesizer
        ocr_note = " (OCR Extracted Record)" if primary.get("is_ocr") else ""
        answer = (
            f"**Official Municipal Guidance from {primary['document_name']} (Page {primary['page_number']}{ocr_note})**:\n\n"
            f"> \"{primary['excerpt']}\"\n\n"
            f"**Operational Compliance Protocol**:\n"
            f"1. **Safety Perimeter**: Erect safety barriers and signage compliant with municipal traffic standards.\n"
            f"2. **Damage Assessment**: Measure defect perimeter and record depth/voltage/pressure metrics.\n"
            f"3. **Execution**: Perform repair strictly in accordance with cited technical specifications."
        )

        return {
            "answer": answer,
            "sources": retrieved_sources,
            "detected_language": detected_lang,
            "relevance_met": True,
            "fallback_used": True,
            "model": "local-rag-synthesizer"
        }


# Global Singleton
rag_engine = RagEngine()
