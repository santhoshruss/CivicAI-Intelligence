import tempfile
import shutil
from pathlib import Path
from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from app.rag.rag_engine import rag_engine

router = APIRouter()

class QueryRequest(BaseModel):
    query: str
    category: Optional[str] = None

@router.post("/upload")
async def upload_document(
    file: UploadFile = File(...),
    category: str = Form("GENERAL"),
    title: str = Form("")
):
    if not file.filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="Only PDF files are supported.")

    temp_path = None
    try:
        suffix = Path(file.filename).suffix
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            shutil.copyfileobj(file.file, tmp)
            temp_path = tmp.name

        doc_title = title or file.filename
        chunk_count = rag_engine.ingest_document(
            file_path=temp_path,
            category=category,
            title=doc_title
        )

        return {
            "status": "SUCCESS",
            "title": doc_title,
            "category": category,
            "chunk_count": chunk_count,
            "indexed_status": "INDEXED"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to ingest document: {str(e)}")
    finally:
        if temp_path and Path(temp_path).exists():
            try:
                Path(temp_path).unlink()
            except Exception:
                pass

@router.post("/query")
async def query_knowledge(req: QueryRequest):
    try:
        result = rag_engine.generate_grounded_answer(
            query=req.query,
            category=req.category
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
