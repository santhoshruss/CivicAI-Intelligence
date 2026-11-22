from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from app.agent.agent_manager import agent_manager

router = APIRouter()

class AgentRunRequest(BaseModel):
    incident_id: int
    reference_number: Optional[str] = None
    category: str
    description: Optional[str] = ""
    latitude: Optional[float] = 37.7749
    longitude: Optional[float] = -122.4194
    address: Optional[str] = ""
    candidates: Optional[List[Dict[str, Any]]] = []
    image_base64: Optional[str] = None
    image_path: Optional[str] = None

@router.post("/run")
async def run_agent_workflow(req: AgentRunRequest):
    """
    Executes the 8-tool Agentic AI triage workflow using actual database records,
    real evidence image inference, Sentence Transformer duplicate detection, and ML priority prediction.
    """
    try:
        result = agent_manager.run_workflow(req.model_dump())
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Agent workflow execution error: {str(e)}")
