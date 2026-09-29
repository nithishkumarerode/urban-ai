from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from geoalchemy2.shape import to_shape, from_shape
from shapely.geometry import shape, mapping, Polygon, MultiPolygon
from backend.database.database import get_db
from backend.database.models import GisLayer, Parcel, Building, Road, LandUse, User
from backend.api.auth import get_current_user
from backend.gis.geometry import calculate_real_area_and_perimeter, check_building_parcel_containment
from backend.services.audit import log_audit_event

router = APIRouter(prefix="/gis", tags=["GIS Engine & Vector Editor"])

class ParcelCreateRequest(BaseModel):
    parcel_identifier: str
    dataset_id: Optional[int] = None
    geometry: Dict[str, Any]
    source: Optional[str] = "User-drawn geometry"

class ParcelUpdateRequest(BaseModel):
    parcel_identifier: Optional[str] = None
    geometry: Optional[Dict[str, Any]] = None
    verification_status: Optional[str] = None

@router.get("/layers")
async def get_gis_layers(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(GisLayer).where(GisLayer.is_active == True)
    res = await db.execute(stmt)
    layers = res.scalars().all()
    return [
        {
            "id": l.id,
            "dataset_id": l.dataset_id,
            "layer_name": l.layer_name,
            "layer_type": l.layer_type,
            "feature_count": l.feature_count,
            "crs": l.crs or "CRS: Not Available"
        }
        for l in layers
    ]

@router.get("/parcels")
async def get_parcels(
    dataset_id: Optional[int] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Parcel)
    if dataset_id:
        stmt = stmt.where(Parcel.dataset_id == dataset_id)
    res = await db.execute(stmt)
    parcels = res.scalars().all()

    out = []
    for p in parcels:
        geom_shape = to_shape(p.geom)
        out.append({
            "id": p.id,
            "parcel_identifier": p.parcel_identifier,
            "dataset_id": p.dataset_id,
            "area_sqm": p.area_sqm,
            "area_hectares": p.area_hectares,
            "perimeter_m": p.perimeter_m,
            "boundary_source": p.boundary_source,
            "ai_confidence": p.ai_confidence,
            "verification_status": p.verification_status,
            "geometry": mapping(geom_shape)
        })
    return out

@router.post("/parcels")
async def create_parcel(
    req: ParcelCreateRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Calculate real area & perimeter in projected metric CRS
    try:
        area_sqm, area_ha, perimeter_m = calculate_real_area_and_perimeter(req.geometry)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"INVALID GEOMETRY: {str(e)}")

    shply_geom = shape(req.geometry)
    if isinstance(shply_geom, Polygon):
        shply_geom = MultiPolygon([shply_geom])

    new_parcel = Parcel(
        parcel_identifier=req.parcel_identifier,
        dataset_id=req.dataset_id,
        geom=from_shape(shply_geom, srid=4326),
        area_sqm=area_sqm,
        area_hectares=area_ha,
        perimeter_m=perimeter_m,
        boundary_source=req.source or "User-drawn geometry",
        verification_status="Pending",
        created_by=current_user.id
    )
    db.add(new_parcel)
    await db.commit()
    await db.refresh(new_parcel)

    await log_audit_event(
        db, action="PARCEL_CREATE", user_id=current_user.id, user_email=current_user.email,
        entity_type="Parcel", entity_id=str(new_parcel.id),
        details={"identifier": new_parcel.parcel_identifier, "area_sqm": area_sqm},
        ip_address=request.client.host if request.client else None
    )

    return {
        "id": new_parcel.id,
        "parcel_identifier": new_parcel.parcel_identifier,
        "area_sqm": new_parcel.area_sqm,
        "area_hectares": new_parcel.area_hectares,
        "perimeter_m": new_parcel.perimeter_m,
        "boundary_source": new_parcel.boundary_source,
        "verification_status": new_parcel.verification_status
    }

@router.put("/parcels/{parcel_id}")
async def update_parcel(
    parcel_id: int,
    req: ParcelUpdateRequest,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Parcel).where(Parcel.id == parcel_id)
    res = await db.execute(stmt)
    parcel = res.scalars().first()
    if not parcel:
        raise HTTPException(status_code=404, detail="Parcel not found")

    if req.parcel_identifier:
        parcel.parcel_identifier = req.parcel_identifier

    if req.verification_status:
        parcel.verification_status = req.verification_status

    if req.geometry:
        area_sqm, area_ha, perimeter_m = calculate_real_area_and_perimeter(req.geometry)
        shply_geom = shape(req.geometry)
        if isinstance(shply_geom, Polygon):
            shply_geom = MultiPolygon([shply_geom])
        parcel.geom = from_shape(shply_geom, srid=4326)
        parcel.area_sqm = area_sqm
        parcel.area_hectares = area_ha
        parcel.perimeter_m = perimeter_m
        parcel.verification_status = "Edited"

    await db.commit()

    await log_audit_event(
        db, action="PARCEL_EDIT", user_id=current_user.id, user_email=current_user.email,
        entity_type="Parcel", entity_id=str(parcel.id),
        ip_address=request.client.host if request.client else None
    )

    return {"message": "Parcel updated successfully"}

@router.delete("/parcels/{parcel_id}")
async def delete_parcel(
    parcel_id: int,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Parcel).where(Parcel.id == parcel_id)
    res = await db.execute(stmt)
    parcel = res.scalars().first()
    if not parcel:
        raise HTTPException(status_code=404, detail="Parcel not found")

    await db.delete(parcel)
    await db.commit()

    await log_audit_event(
        db, action="PARCEL_DELETE", user_id=current_user.id, user_email=current_user.email,
        entity_type="Parcel", entity_id=str(parcel_id),
        ip_address=request.client.host if request.client else None
    )

    return {"message": "Parcel deleted"}

@router.get("/buildings")
async def get_buildings(
    dataset_id: Optional[int] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Building)
    if dataset_id:
        stmt = stmt.where(Building.dataset_id == dataset_id)
    res = await db.execute(stmt)
    buildings = res.scalars().all()

    return [
        {
            "id": b.id,
            "building_code": b.building_code,
            "parcel_id": b.parcel_id,
            "area_sqm": b.area_sqm,
            "estimated_height_m": b.estimated_height_m,
            "ai_confidence": b.ai_confidence,
            "verification_status": b.verification_status,
            "geometry": mapping(to_shape(b.geom))
        }
        for b in buildings
    ]

@router.get("/roads")
async def get_roads(
    dataset_id: Optional[int] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Road)
    if dataset_id:
        stmt = stmt.where(Road.dataset_id == dataset_id)
    res = await db.execute(stmt)
    roads = res.scalars().all()

    return [
        {
            "id": r.id,
            "road_identifier": r.road_identifier,
            "length_m": r.length_m,
            "buffer_width_m": r.buffer_width_m,
            "verification_status": r.verification_status,
            "geometry": mapping(to_shape(r.geom))
        }
        for r in roads
    ]

@router.get("/land-use")
async def get_land_use(
    dataset_id: Optional[int] = None,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(LandUse)
    if dataset_id:
        stmt = stmt.where(LandUse.dataset_id == dataset_id)
    res = await db.execute(stmt)
    items = res.scalars().all()

    return [
        {
            "id": lu.id,
            "class_code": lu.class_code,
            "class_name": lu.class_name,
            "area_sqm": lu.area_sqm,
            "geometry": mapping(to_shape(lu.geom))
        }
        for lu in items
    ]

@router.get("/adjacent-check")
async def check_adjacent_parcel(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Section 17: 'Create Adjacent Parcel' must only work when a real existing parcel geometry exists.
    If no parcel exists: 'No existing parcel geometry. Upload GIS data or draw the first parcel.'
    """
    stmt = select(Parcel).limit(1)
    res = await db.execute(stmt)
    has_parcel = res.scalars().first() is not None
    if not has_parcel:
        return {
            "allowed": False,
            "message": "No existing parcel geometry. Upload GIS data or draw the first parcel."
        }
    return {
        "allowed": True,
        "message": "Ready to snap adjacent parcel boundary."
    }
