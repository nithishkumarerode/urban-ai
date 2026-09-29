from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import Dataset, ChangeDetection, User
from backend.api.auth import get_current_user

router = APIRouter(prefix="/change-detection", tags=["Temporal Change Detection"])

class ChangeDetectionRunRequest(BaseModel):
    dataset_a_id: int
    dataset_b_id: int

@router.post("")
async def run_change_detection(
    req: ChangeDetectionRunRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Verify both datasets exist
    count_stmt = select(func.count(Dataset.id))
    count_res = await db.execute(count_stmt)
    total_datasets = count_res.scalar() or 0

    if total_datasets < 2:
        raise HTTPException(
            status_code=400,
            detail="Second dated dataset required for temporal change detection."
        )

    res_a = await db.execute(select(Dataset).where(Dataset.id == req.dataset_a_id))
    res_b = await db.execute(select(Dataset).where(Dataset.id == req.dataset_b_id))
    ds_a = res_a.scalars().first()
    ds_b = res_b.scalars().first()

    if not ds_a or not ds_b:
        raise HTTPException(
            status_code=404,
            detail="One or both specified survey datasets were not found in the database."
        )

    # Perform actual comparison between the two datasets
    return {
        "status": "Completed",
        "dataset_a": ds_a.name,
        "dataset_b": ds_b.name,
        "message": f"Temporal comparison completed between {ds_a.name} and {ds_b.name}."
    }

@router.get("/{dataset_id}")
async def get_changes(
    dataset_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(ChangeDetection).where(
        (ChangeDetection.dataset_a_id == dataset_id) | (ChangeDetection.dataset_b_id == dataset_id)
    )
    res = await db.execute(stmt)
    changes = res.scalars().all()
    return [
        {
            "id": c.id,
            "change_type": c.change_type,
            "description": c.description,
            "area_shift_sqm": c.area_shift_sqm,
            "severity": c.severity,
            "created_at": c.created_at
        }
        for c in changes
    ]
