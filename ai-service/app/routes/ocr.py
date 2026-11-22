from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
from app.services.ocr_service import ocr_service
from app.rag.rag_engine import rag_engine

router = APIRouter()

class IngestOcrRequest(BaseModel):
    title: str
    category: str = "GENERAL"
    pages: List[Dict[str, Any]]

@router.post("/ocr-document")
async def ocr_document(file: UploadFile = File(...)):
    """
    Extracts text from uploaded PDF or image maintenance document using PyMuPDF and RapidOCR.
    Extracts document metadata and page text.
    """
    try:
        file_bytes = await file.read()
        res = ocr_service.process_document(file_bytes, file.filename or "doc.pdf")
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Document OCR processing error: {str(e)}")

@router.post("/ingest-ocr")
async def ingest_ocr(req: IngestOcrRequest):
    """
    Ingests reviewed OCR pages directly into the FAISS RAG vector index.
    """
    try:
        chunk_count = rag_engine.ingest_ocr_pages(
            pages=req.pages,
            category=req.category,
            title=req.title
        )
        return {
            "status": "SUCCESS",
            "title": req.title,
            "category": req.category,
            "chunk_count": chunk_count,
            "indexed_status": "INDEXED"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"OCR ingestion error: {str(e)}")
