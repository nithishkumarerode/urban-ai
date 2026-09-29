from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from geoalchemy2.shape import to_shape
from shapely.geometry import mapping
import json
from backend.database.database import get_db
from backend.database.models import Dataset, RasterMetadata, Parcel, Building, Road, User
from backend.api.auth import get_current_user
from backend.ai.inference import check_ai_model_availability

router = APIRouter(prefix="/reports", tags=["Cadastral Reports & Export"])

@router.get("/geojson/{dataset_id}")
async def export_geojson(
    dataset_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Parcel).where(Parcel.dataset_id == dataset_id)
    res = await db.execute(stmt)
    parcels = res.scalars().all()

    features = []
    for p in parcels:
        features.append({
            "type": "Feature",
            "properties": {
                "parcel_identifier": p.parcel_identifier,
                "area_sqm": p.area_sqm,
                "area_hectares": p.area_hectares,
                "perimeter_m": p.perimeter_m,
                "boundary_source": p.boundary_source,
                "verification_status": p.verification_status
            },
            "geometry": mapping(to_shape(p.geom))
        })

    geojson_doc = {
        "type": "FeatureCollection",
        "crs": {"type": "name", "properties": {"name": "urn:ogc:def:crs:OGC:1.3:CRS84"}},
        "features": features
    }

    return Response(
        content=json.dumps(geojson_doc, indent=2),
        media_type="application/geo+json",
        headers={"Content-Disposition": f"attachment; filename=cadastre_dataset_{dataset_id}.geojson"}
    )

@router.get("/summary/{dataset_id}")
async def export_summary_report(
    dataset_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    ds_stmt = select(Dataset).where(Dataset.id == dataset_id)
    ds_res = await db.execute(ds_stmt)
    dataset = ds_res.scalars().first()
    if not dataset:
        raise HTTPException(status_code=404, detail="Dataset not found")

    meta_stmt = select(RasterMetadata).where(RasterMetadata.dataset_id == dataset_id)
    meta_res = await db.execute(meta_stmt)
    meta = meta_res.scalars().first()

    p_stmt = select(Parcel).where(Parcel.dataset_id == dataset_id)
    p_res = await db.execute(p_stmt)
    parcels = p_res.scalars().all()

    b_stmt = select(Building).where(Building.dataset_id == dataset_id)
    b_res = await db.execute(b_stmt)
    buildings = b_res.scalars().all()

    r_stmt = select(Road).where(Road.dataset_id == dataset_id)
    r_res = await db.execute(r_stmt)
    roads = r_res.scalars().all()

    is_model_avail, model_msg = check_ai_model_availability()

    total_area_sqm = sum(p.area_sqm for p in parcels)

    return {
        "report_title": "AI-Assisted Preliminary Cadastral Mapping Summary",
        "legal_notice": "This document represents AI-assisted preliminary cadastral mapping and validation. It does NOT claim to determine legal land ownership from aerial imagery.",
        "dataset": {
            "id": dataset.id,
            "name": dataset.name,
            "file_type": dataset.file_type,
            "crs": meta.crs if (meta and meta.crs) else "CRS: Not Available",
            "gsd": f"{meta.gsd_cm} cm/px" if (meta and meta.gsd_cm is not None) else "GSD: Not Available",
            "dimensions": f"{meta.width}x{meta.height} px" if meta else "Not Available"
        },
        "cadastral_metrics": {
            "total_mapped_area_sqm": round(total_area_sqm, 2),
            "total_mapped_area_hectares": round(total_area_sqm / 10000.0, 4),
            "parcel_count": len(parcels),
            "building_count": len(buildings),
            "road_count": len(roads)
        },
        "ai_model_information": {
            "status": "AVAILABLE" if is_model_avail else "AI MODEL NOT AVAILABLE",
            "details": model_msg
        },
        "verification_breakdown": {
            "pending": sum(1 for p in parcels if p.verification_status == "Pending"),
            "accepted": sum(1 for p in parcels if p.verification_status == "Accepted"),
            "rejected": sum(1 for p in parcels if p.verification_status == "Rejected"),
            "edited": sum(1 for p in parcels if p.verification_status == "Edited")
        }
    }
