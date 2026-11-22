"""
CivicAI Speech-to-Text Service using Hugging Face Whisper
Supports audio transcription for citizen complaint registration in English and Tamil.
Optimized for local CPU execution with soundfile decoding and browser fallback.
"""
import os
import io
import tempfile
from typing import Dict, Any, Optional
import numpy as np

HF_WHISPER_MODEL = os.getenv("HF_WHISPER_MODEL", "openai/whisper-tiny")
ENABLE_HF_SPEECH = os.getenv("ENABLE_HF_SPEECH", "true").lower() == "true"

try:
    import soundfile as sf
    SOUNDFILE_AVAILABLE = True
except ImportError:
    SOUNDFILE_AVAILABLE = False

try:
    from transformers import pipeline
    TRANSFORMERS_SPEECH_AVAILABLE = True
except ImportError:
    TRANSFORMERS_SPEECH_AVAILABLE = False


class SpeechTranscriptionService:
    def __init__(self):
        self.asr_pipeline = None
        self.model_name = HF_WHISPER_MODEL
        self._init_pipeline()

    def _init_pipeline(self):
        """Lazy loader for Hugging Face ASR pipeline."""
        if ENABLE_HF_SPEECH and TRANSFORMERS_SPEECH_AVAILABLE:
            try:
                # whisper-tiny / whisper-small pipeline on CPU
                self.asr_pipeline = pipeline(
                    "automatic-speech-recognition",
                    model=self.model_name,
                    device=-1  # CPU
                )
                print(f"[SpeechService] Loaded Whisper ASR pipeline: {self.model_name}")
            except Exception as e:
                print(f"[SpeechService] Whisper ASR pipeline load warning: {e}. Fallback active.")
                self.asr_pipeline = None
        else:
            self.asr_pipeline = None

    def transcribe_audio(self, audio_bytes: bytes, filename: str = "voice.wav") -> Dict[str, Any]:
        """
        Transcribes audio bytes to text using Whisper.
        Accepts WAV, WebM, OGG, or MP3 data.
        """
        if not audio_bytes or len(audio_bytes) < 64:
            return {
                "transcription": "",
                "status": "EMPTY_AUDIO",
                "message": "Recorded audio was empty or too short."
            }

        if self.asr_pipeline is None:
            return {
                "transcription": "",
                "status": "FALLBACK_TO_BROWSER",
                "message": "Hugging Face Whisper model not pre-loaded on host. Use browser speech recognition fallback."
            }

        try:
            # Read audio using soundfile
            audio_io = io.BytesIO(audio_bytes)
            audio_data, sample_rate = sf.read(audio_io)

            # If stereo, average to mono
            if len(audio_data.shape) > 1:
                audio_data = np.mean(audio_data, axis=1)

            # If sample rate != 16000, resample or pass audio dict
            # Transformers pipeline accepts {"raw": np.ndarray, "sampling_rate": int}
            input_audio = {
                "raw": audio_data.astype(np.float32),
                "sampling_rate": sample_rate
            }

            result = self.asr_pipeline(input_audio, return_timestamps=False)
            transcription = result.get("text", "").strip()

            return {
                "transcription": transcription,
                "model_name": f"HuggingFace/{self.model_name}",
                "sample_rate": sample_rate,
                "duration_seconds": round(len(audio_data) / sample_rate, 2),
                "status": "SUCCESS"
            }
        except Exception as e:
            print(f"[SpeechService] Transcription error: {e}")
            return {
                "transcription": "",
                "status": "ERROR",
                "message": f"Speech transcription error: {str(e)}"
            }


# Global singleton instance
speech_service = SpeechTranscriptionService()
