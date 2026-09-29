from typing import List, Optional
from fastapi import APIRouter, Depends
from sqlalchemy import select, or_
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import Parcel, Building, Dataset, User
from backend.api.auth import get_current_user

router = APIRouter(prefix="/search", tags=["GIS Search"])

@router.get("")
async def search_cadastre(
    q: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if not q or len(q.strip()) < 2:
        return {"parcels": [], "buildings": [], "datasets": []}

    search_term = f"%{q.strip()}%"

    # Search Parcels
    p_stmt = select(Parcel).where(Parcel.parcel_identifier.ilike(search_term)).limit(10)
    p_res = await db.execute(p_stmt)
    parcels = p_res.scalars().all()

    # Search Buildings
    b_stmt = select(Building).where(Building.building_code.ilike(search_term)).limit(10)
    b_res = await db.execute(b_stmt)
    buildings = b_res.scalars().all()

    # Search Datasets
    d_stmt = select(Dataset).where(
        or_(Dataset.name.ilike(search_term), Dataset.filename.ilike(search_term))
    ).limit(10)
    d_res = await db.execute(d_stmt)
    datasets = d_res.scalars().all()

    return {
        "parcels": [
            {
                "id": p.id,
                "parcel_identifier": p.parcel_identifier,
                "area_sqm": p.area_sqm,
                "status": p.verification_status
            }
            for p in parcels
        ],
        "buildings": [
            {
                "id": b.id,
                "building_code": b.building_code,
                "parcel_id": b.parcel_id,
                "area_sqm": b.area_sqm
            }
            for b in buildings
        ],
        "datasets": [
            {
                "id": d.id,
                "name": d.name,
                "file_type": d.file_type
            }
            for d in datasets
        ]
    }
