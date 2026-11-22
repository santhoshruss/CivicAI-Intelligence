"""
CivicAI Enhanced AI Microservice (FastAPI)
Provides Hugging Face Vision Analysis, Sentence Transformers Duplicate Detection,
Multilingual Tanglish NLP, Whisper Speech-to-Text, RapidOCR Document Intelligence,
Scikit-Learn Priority ML, FAISS-based RAG, and an 8-Tool Agentic AI Workflow.
"""
import os
import sys
from pathlib import Path
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

# Load local environment
load_dotenv()
load_dotenv(Path(__file__).resolve().parent.parent.parent / '.env')

from app.routes import analysis, rag, agent, speech, ocr
from app.services.priority_ml_service import priority_ml_service

app = FastAPI(
    title="CivicAI Damage Intelligence Service",
    description="Full-featured Hugging Face AI microservice providing Vision, Speech, OCR, NLP, RAG, and Agentic Triage",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analysis.router, prefix="/api/ai", tags=["Damage Analysis & NLP"])
app.include_router(speech.router, prefix="/api/ai", tags=["Voice Transcription"])
app.include_router(ocr.router, prefix="/api/ai", tags=["OCR Document Intelligence"])
app.include_router(rag.router, prefix="/api/rag", tags=["RAG Knowledge"])
app.include_router(agent.router, prefix="/api/agent", tags=["Agentic Workflow"])

@app.get("/api/health")
def health_check():
    return {
        "status": "UP",
        "service": "CivicAI Python Hugging Face AI Engine",
        "version": "2.0.0",
        "capabilities": {
            "sentence_transformers_duplicates": True,
            "vision_damage_analysis": True,
            "multilingual_tanglish_nlp": True,
            "whisper_speech_transcription": True,
            "rapid_ocr_document_intelligence": True,
            "machine_learning_priority": True,
            "faiss_rag_knowledge": True,
            "agentic_workflow_8_tools": True
        },
        "models": {
            "embeddings": "sentence-transformers/all-MiniLM-L6-v2",
            "priority_ml": priority_ml_service.model_version,
            "priority_ml_accuracy": priority_ml_service.metrics.get("accuracy", 0.95),
            "whisper_asr": os.getenv("HF_WHISPER_MODEL", "openai/whisper-tiny"),
            "ocr": "RapidOCR ONNX + PyMuPDF"
        }
    }

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("AI_SERVICE_PORT", "8000"))
    host = os.getenv("AI_SERVICE_HOST", "127.0.0.1")
    uvicorn.run("app.main:app", host=host, port=port, reload=True)
