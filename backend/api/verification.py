from typing import Optional, Dict, Any
from datetime import datetime
from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import VerificationTask, Parcel, User
from backend.api.auth import get_current_user
from backend.services.audit import log_audit_event

router = APIRouter(prefix="/verification", tags=["Human Verification Queue"])

class VerificationDecisionRequest(BaseModel):
    notes: Optional[str] = None
    rejection_reason: Optional[str] = None

@router.get("")
async def list_verification_tasks(
    status_filter: Optional[str] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(VerificationTask)
    if status_filter:
        stmt = stmt.where(VerificationTask.status == status_filter)
    stmt = stmt.order_by(VerificationTask.created_at.desc())
    res = await db.execute(stmt)
    tasks = res.scalars().all()
    return [
        {
            "id": t.id,
            "feature_type": t.feature_type,
            "feature_id": t.feature_id,
            "dataset_id": t.dataset_id,
            "status": t.status,
            "original_geometry": t.original_geometry,
            "edited_geometry": t.edited_geometry,
            "rejection_reason": t.rejection_reason,
            "notes": t.notes,
            "reviewed_at": t.reviewed_at,
            "created_at": t.created_at
        }
        for t in tasks
    ]

@router.post("/{task_id}/accept")
async def accept_verification_task(
    task_id: int,
    req: VerificationDecisionRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(VerificationTask).where(VerificationTask.id == task_id)
    res = await db.execute(stmt)
    task = res.scalars().first()
    if not task:
        raise HTTPException(status_code=404, detail="Verification task not found")

    task.status = "Accepted"
    task.assigned_user_id = current_user.id
    task.notes = req.notes
    task.reviewed_at = datetime.utcnow()

    # Update corresponding parcel or feature
    if task.feature_type == "parcel":
        p_stmt = select(Parcel).where(Parcel.id == task.feature_id)
        p_res = await db.execute(p_stmt)
        parcel = p_res.scalars().first()
        if parcel:
            parcel.verification_status = "Accepted"

    await db.commit()

    await log_audit_event(
        db, action="VERIFICATION_ACCEPTED", user_id=current_user.id, user_email=current_user.email,
        entity_type=task.feature_type, entity_id=str(task.feature_id),
        details={"task_id": task.id, "status": "Accepted"},
        ip_address=request.client.host if request.client else None
    )

    return {"message": f"Feature {task.feature_id} accepted successfully"}

@router.post("/{task_id}/reject")
async def reject_verification_task(
    task_id: int,
    req: VerificationDecisionRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(VerificationTask).where(VerificationTask.id == task_id)
    res = await db.execute(stmt)
    task = res.scalars().first()
    if not task:
        raise HTTPException(status_code=404, detail="Verification task not found")

    task.status = "Rejected"
    task.assigned_user_id = current_user.id
    task.rejection_reason = req.rejection_reason or "Boundary rejected by cadastral analyst"
    task.notes = req.notes
    task.reviewed_at = datetime.utcnow()

    if task.feature_type == "parcel":
        p_stmt = select(Parcel).where(Parcel.id == task.feature_id)
        p_res = await db.execute(p_stmt)
        parcel = p_res.scalars().first()
        if parcel:
            parcel.verification_status = "Rejected"

    await db.commit()

    await log_audit_event(
        db, action="VERIFICATION_REJECTED", user_id=current_user.id, user_email=current_user.email,
        entity_type=task.feature_type, entity_id=str(task.feature_id),
        details={"task_id": task.id, "reason": task.rejection_reason},
        ip_address=request.client.host if request.client else None
    )

    return {"message": f"Feature {task.feature_id} rejected"}
