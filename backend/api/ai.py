from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import Dataset, AiPrediction, User
from backend.api.auth import get_current_user
from backend.ai.inference import check_ai_model_availability, run_segmentation_inference, AiModelUnavailableException
from backend.services.audit import log_audit_event

router = APIRouter(prefix="/ai", tags=["AI Inference Pipeline"])

@router.get("/status/{dataset_id}")
async def get_ai_status(
    dataset_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    is_available, model_msg = check_ai_model_availability()
    
    stmt = select(AiPrediction).where(AiPrediction.dataset_id == dataset_id).order_by(AiPrediction.id.desc())
    res = await db.execute(stmt)
    latest_job = res.scalars().first()

    return {
        "dataset_id": dataset_id,
        "model_availability": "AVAILABLE" if is_available else "AI MODEL NOT AVAILABLE",
        "model_status_message": model_msg,
        "latest_job": {
            "id": latest_job.id,
            "status": latest_job.status,
            "total_tiles": latest_job.total_tiles,
            "processed_tiles": latest_job.processed_tiles,
            "features_extracted_count": latest_job.features_extracted_count,
            "error_message": latest_job.error_message
        } if latest_job else None
    }

@router.post("/scan/{dataset_id}")
async def run_ai_scan(
    dataset_id: int,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Dataset).where(Dataset.id == dataset_id)
    result = await db.execute(stmt)
    dataset = result.scalars().first()
    if not dataset:
        raise HTTPException(status_code=404, detail="NO GIS DATA LOADED")

    is_available, model_msg = check_ai_model_availability()
    if not is_available:
        # Strictly report missing model - DO NOT invent fake results!
        raise HTTPException(
            status_code=503,
            detail="AI MODEL NOT AVAILABLE. Model weights (.onnx) must be placed in backend/models/weights/."
        )

    try:
        scan_res = run_segmentation_inference(dataset.file_path)
        job = AiPrediction(
            dataset_id=dataset_id,
            model_name="CadastralSegmenter-ONNX",
            status="Completed",
            features_extracted_count=scan_res.get("features_extracted_count", 0)
        )
        db.add(job)
        await db.commit()

        await log_audit_event(
            db, action="AI_PROCESSING", user_id=current_user.id, user_email=current_user.email,
            entity_type="Dataset", entity_id=str(dataset_id),
            details={"status": "Completed"},
            ip_address=request.client.host if request.client else None
        )

        return {"status": "Completed", "details": scan_res}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI PROCESSING FAILED: {str(e)}")
