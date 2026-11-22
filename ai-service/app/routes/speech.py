from fastapi import APIRouter, UploadFile, File, HTTPException
from app.services.speech_service import speech_service
from app.services.nlp_service import nlp_service

router = APIRouter()

@router.post("/transcribe-audio")
async def transcribe_audio(file: UploadFile = File(...)):
    """
    Accepts recorded voice audio (WAV/WebM/MP3), executes Hugging Face Whisper ASR,
    and runs multilingual NLP intent extraction on the transcription.
    """
    try:
        audio_bytes = await file.read()
        transcription_res = speech_service.transcribe_audio(
            audio_bytes=audio_bytes,
            filename=file.filename or "recording.wav"
        )

        transcription_text = transcription_res.get("transcription", "")
        nlp_analysis = {}
        if transcription_text:
            nlp_analysis = nlp_service.classify_complaint(transcription_text)

        return {
            "transcription": transcription_text,
            "status": transcription_res.get("status", "SUCCESS"),
            "model_name": transcription_res.get("model_name", "Whisper"),
            "duration_seconds": transcription_res.get("duration_seconds", 0),
            "nlp_analysis": nlp_analysis
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Audio transcription error: {str(e)}")
