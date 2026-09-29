from fastapi import APIRouter, Depends
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import Dataset, Parcel, Building, Road, LandUse, ChangeDetection, AiPrediction, User
from backend.api.auth import get_current_user
from backend.ai.inference import check_ai_model_availability

router = APIRouter(prefix="/dashboard", tags=["Dashboard Statistics"])

@router.get("/statistics")
async def get_dashboard_statistics(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Total datasets
    dataset_count_res = await db.execute(select(func.count(Dataset.id)))
    total_datasets = dataset_count_res.scalar() or 0

    if total_datasets == 0:
        is_model_avail, model_msg = check_ai_model_availability()
        return {
            "has_data": False,
            "message": "No GIS dataset available.",
            "total_datasets": 0,
            "total_parcels": 0,
            "buildings_detected": 0,
            "roads_detected": 0,
            "vegetation_areas_count": 0,
            "water_areas_count": 0,
            "total_mapped_area_sqm": 0.0,
            "total_mapped_area_hectares": 0.0,
            "changes_detected": 0,
            "ai_model_status": "AVAILABLE" if is_model_avail else "AI MODEL NOT AVAILABLE",
            "ai_model_message": model_msg,
            "gis_dataset_status": "NO GIS DATA LOADED",
            "last_processing_time": None
        }

    # Total parcels
    parcel_count_res = await db.execute(select(func.count(Parcel.id)))
    total_parcels = parcel_count_res.scalar() or 0

    # Total buildings
    bldg_count_res = await db.execute(select(func.count(Building.id)))
    buildings_detected = bldg_count_res.scalar() or 0

    # Total roads
    roads_count_res = await db.execute(select(func.count(Road.id)))
    roads_detected = roads_count_res.scalar() or 0

    # Land use breakdown
    veg_res = await db.execute(select(func.count(LandUse.id)).where(LandUse.class_code == 3))
    vegetation_count = veg_res.scalar() or 0

    water_res = await db.execute(select(func.count(LandUse.id)).where(LandUse.class_code == 4))
    water_count = water_res.scalar() or 0

    # Total mapped area
    area_res = await db.execute(select(func.sum(Parcel.area_sqm)))
    total_area_sqm = area_res.scalar() or 0.0
    total_area_ha = round(total_area_sqm / 10000.0, 4)

    # Changes detected
    change_res = await db.execute(select(func.count(ChangeDetection.id)))
    changes_detected = change_res.scalar() or 0

    # AI Model status
    is_model_avail, model_msg = check_ai_model_availability()

    # Last processing time
    job_res = await db.execute(select(AiPrediction.completed_at).order_by(AiPrediction.id.desc()).limit(1))
    last_proc_time = job_res.scalar()

    return {
        "has_data": True,
        "message": "GIS datasets active and synchronized.",
        "total_datasets": total_datasets,
        "total_parcels": total_parcels,
        "buildings_detected": buildings_detected,
        "roads_detected": roads_detected,
        "vegetation_areas_count": vegetation_count,
        "water_areas_count": water_count,
        "total_mapped_area_sqm": round(total_area_sqm, 2),
        "total_mapped_area_hectares": total_area_ha,
        "changes_detected": changes_detected,
        "ai_model_status": "AVAILABLE" if is_model_avail else "AI MODEL NOT AVAILABLE",
        "ai_model_message": model_msg,
        "gis_dataset_status": f"{total_datasets} datasets loaded",
        "last_processing_time": last_proc_time
    }
