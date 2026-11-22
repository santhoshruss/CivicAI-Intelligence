from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from typing import Optional, List, Dict, Any
from app.services.vision_service import vision_service
from app.services.duplicate_service import duplicate_service
from app.services.nlp_service import nlp_service
from app.services.priority_ml_service import priority_ml_service

router = APIRouter()

@router.post("/analyze-image")
async def analyze_image(
    file: Optional[UploadFile] = File(None),
    category: str = Form("POTHOLE"),
    description: str = Form("")
):
    """
    Accepts uploaded damage evidence and runs Hugging Face vision inference
    with region-of-interest localization and labeled heuristic fallback.
    """
    try:
        image_bytes = b""
        if file is not None:
            image_bytes = await file.read()

        result = vision_service.analyze_image(
            image_bytes=image_bytes,
            reported_category=category,
            description=description
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/detect-duplicates")
async def detect_duplicates(payload: dict):
    """
    Evaluates candidate incidents against target using Hugging Face Sentence Transformers
    (all-MiniLM-L6-v2), Haversine GPS distance, and category alignment.
    """
    target = payload.get("target") or payload
    candidates = payload.get("candidates", [])
    radius_meters = float(payload.get("radius_meters", 300.0))

    try:
        matches = duplicate_service.evaluate_duplicate_candidates(
            target_incident=target,
            candidates=candidates,
            radius_meters=radius_meters
        )
        return {"matches": matches, "total_candidates_evaluated": len(candidates)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Duplicate detection error: {str(e)}")


@router.post("/process-complaint")
async def process_complaint(payload: dict):
    """
    Multilingual NLP processing for English, Tamil script, and Tanglish.
    Performs language identification, transliteration/normalization, category and urgency classification.
    """
    text = payload.get("text", "")
    try:
        result = nlp_service.classify_complaint(text)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"NLP processing error: {str(e)}")


@router.post("/predict-priority")
async def predict_priority(payload: dict):
    """
    Predicts municipal incident priority using validated Scikit-Learn classifier
    with feature explanation and rule-based fallback.
    """
    category = payload.get("category", "POTHOLE")
    description = payload.get("description", "")
    confidence = float(payload.get("confidence", 0.85))
    duplicate_count = int(payload.get("duplicate_count", 0))

    try:
        result = priority_ml_service.predict_priority(
            category=category,
            description=description,
            confidence=confidence,
            duplicate_count=duplicate_count
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Priority prediction error: {str(e)}")
