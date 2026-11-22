"""
CivicAI OCR Document Intelligence Service
Processes municipal maintenance SOPs, contractor inspection records, and scanned forms.
Uses PyMuPDF (fitz) for native digital text and RapidOCR / PyTesseract for scanned documents.
Extracts title, department, date, and maintenance protocols for RAG ingestion.
"""
import io
import re
from typing import Dict, Any, List, Optional
from PIL import Image
import numpy as np

try:
    import fitz  # PyMuPDF
    PYMUPDF_AVAILABLE = True
except ImportError:
    PYMUPDF_AVAILABLE = False

try:
    from rapidocr_onnxruntime import RapidOCR
    RAPID_OCR_AVAILABLE = True
except ImportError:
    RAPID_OCR_AVAILABLE = False


class OcrDocumentService:
    def __init__(self):
        self.ocr_engine = None
        if RAPID_OCR_AVAILABLE:
            try:
                self.ocr_engine = RapidOCR()
                print("[OcrService] RapidOCR ONNX engine initialized.")
            except Exception as e:
                print(f"[OcrService] RapidOCR init warning: {e}")

    def extract_text_from_image(self, image_bytes: bytes) -> str:
        """Applies OCR to an image and returns concatenated text."""
        if not image_bytes or len(image_bytes) < 32:
            return ""

        if self.ocr_engine is not None:
            try:
                img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
                img_arr = np.array(img)
                result, _ = self.ocr_engine(img_arr)
                if result:
                    lines = [line[1] for line in result]
                    return "\n".join(lines)
            except Exception as e:
                print(f"[OcrService] Image OCR error: {e}")
        return ""

    def process_document(self, file_bytes: bytes, filename: str) -> Dict[str, Any]:
        """
        Parses document (PDF or Image):
        - Direct extraction from digital PDFs.
        - Fallback OCR on scanned pages or image formats.
        - Extracts title, department, date, and SOP sections.
        """
        is_pdf = filename.lower().endswith(".pdf")
        pages_data: List[Dict[str, Any]] = []
        ocr_applied = False

        if is_pdf and PYMUPDF_AVAILABLE:
            try:
                doc = fitz.open(stream=file_bytes, filetype="pdf")
                for page_idx in range(len(doc)):
                    page = doc[page_idx]
                    page_num = page_idx + 1
                    raw_text = page.get_text("text").strip()

                    # If page has very little digital text, it is likely scanned
                    if len(raw_text) < 40 and self.ocr_engine is not None:
                        pix = page.get_pixmap(dpi=150)
                        img_bytes = pix.tobytes("png")
                        ocr_text = self.extract_text_from_image(img_bytes)
                        if ocr_text:
                            raw_text = ocr_text
                            ocr_applied = True

                    if raw_text:
                        pages_data.append({
                            "page": page_num,
                            "text": raw_text,
                            "is_ocr": ocr_applied
                        })
                doc.close()
            except Exception as e:
                print(f"[OcrService] PDF processing error: {e}")
        else:
            # Assume image document
            text = self.extract_text_from_image(file_bytes)
            if text:
                pages_data.append({
                    "page": 1,
                    "text": text,
                    "is_ocr": True
                })
                ocr_applied = True

        full_text = "\n\n".join(p["text"] for p in pages_data)
        metadata = self._extract_metadata(full_text, filename)

        return {
            "filename": filename,
            "total_pages": len(pages_data),
            "ocr_applied": ocr_applied,
            "metadata": metadata,
            "pages": pages_data,
            "total_character_count": len(full_text)
        }

    def _extract_metadata(self, text: str, filename: str) -> Dict[str, Any]:
        """Heuristically parses title, department, date, and protocol keywords from document."""
        title_match = re.search(r"(?:Standard Operating Procedure|SOP|Manual|Protocol|Guideline)[^\n]+", text, re.I)
        title = title_match.group(0).strip() if title_match else filename.replace("_", " ").replace(".pdf", "")

        dept = "Road Infrastructure Department"
        if "electrical" in text.lower() or "lighting" in text.lower():
            dept = "Electrical & Public Lighting"
        elif "water" in text.lower() or "sewage" in text.lower() or "rupture" in text.lower():
            dept = "Water & Drainage Authority"
        elif "sanitation" in text.lower() or "waste" in text.lower():
            dept = "Sanitation & Waste Management"

        date_match = re.search(r"\b(?:\d{1,2}[-/]\d{1,2}[-/]\d{2,4}|(?:January|February|March|April|May|June|July|August|September|October|November|December)\s+\d{1,2},?\s+\d{4})\b", text)
        doc_date = date_match.group(0) if date_match else "2026 Revision"

        return {
            "title": title[:120],
            "department": dept,
            "document_date": doc_date,
            "contains_safety_protocol": "safety" in text.lower() or "hazard" in text.lower(),
            "contains_slas": "sla" in text.lower() or "hour" in text.lower() or "response time" in text.lower()
        }


# Global singleton instance
ocr_service = OcrDocumentService()
