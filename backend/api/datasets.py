import os
import shutil
import uuid
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, Request
from sqlalchemy import select, delete
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database.database import get_db
from backend.database.models import Dataset, RasterMetadata, GisLayer, User
from backend.api.auth import get_current_user
from backend.gis.raster import inspect_geotiff
from backend.services.audit import log_audit_event

router = APIRouter(prefix="/datasets", tags=["Datasets"])

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "data/uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/upload")
async def upload_dataset(
    request: Request,
    file: UploadFile = File(...),
    dataset_name: Optional[str] = Form(None),
    source_name: Optional[str] = Form(None),
    source_url: Optional[str] = Form(None),
    license: Optional[str] = Form(None),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    orig_filename = file.filename or "unnamed_dataset"
    ext = os.path.splitext(orig_filename)[1].lower()

    if ext not in [".tif", ".tiff", ".geojson", ".json", ".shp", ".gpkg", ".jpg", ".jpeg", ".png"]:
        raise HTTPException(
            status_code=400,
            detail="INVALID OR UNSUPPORTED FILE FORMAT. Supported: .tif, .tiff, .geojson, .shp, .gpkg, .jpg, .png"
        )

    is_georeferenced = ext not in [".jpg", ".jpeg", ".png"]
    warning_msg = None
    if not is_georeferenced:
        warning_msg = "Image has no georeferencing unless supplied separately."

    file_uuid = str(uuid.uuid4())
    stored_filename = f"{file_uuid}_{orig_filename}"
    saved_path = os.path.join(UPLOAD_DIR, stored_filename)

    with open(saved_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    file_size = os.path.getsize(saved_path)

    # Determine file type description
    file_type = "GeoTIFF"
    if ext in [".geojson", ".json"]:
        file_type = "GeoJSON"
    elif ext == ".shp":
        file_type = "Shapefile"
    elif ext == ".gpkg":
        file_type = "GeoPackage"
    elif ext in [".jpg", ".jpeg", ".png"]:
        file_type = "Aerial Image (Unreferenced)"

    new_dataset = Dataset(
        name=dataset_name or orig_filename,
        filename=orig_filename,
        file_type=file_type,
        file_path=saved_path,
        file_size_bytes=file_size,
        user_id=current_user.id,
        source_name=source_name or "Survey Upload",
        source_url=source_url,
        license=license,
        is_georeferenced=is_georeferenced
    )
    db.add(new_dataset)
    await db.commit()
    await db.refresh(new_dataset)

    # Inspect GeoTIFF if applicable
    metadata_out = None
    if ext in [".tif", ".tiff"]:
        raw_meta = inspect_geotiff(saved_path)
        new_meta = RasterMetadata(
            dataset_id=new_dataset.id,
            width=raw_meta.get("width", 0),
            height=raw_meta.get("height", 0),
            bands=raw_meta.get("bands", 0),
            data_type=raw_meta.get("data_type", "unknown"),
            crs=raw_meta.get("crs"), # Never invented
            affine_transform=raw_meta.get("affine_transform"),
            bounds_min_x=raw_meta.get("bounds_min_x"),
            bounds_min_y=raw_meta.get("bounds_min_y"),
            bounds_max_x=raw_meta.get("bounds_max_x"),
            bounds_max_y=raw_meta.get("bounds_max_y"),
            resolution_x=raw_meta.get("resolution_x"),
            resolution_y=raw_meta.get("resolution_y"),
            gsd_cm=raw_meta.get("gsd_cm"), # Never invented
            nodata_value=raw_meta.get("nodata_value")
        )
        db.add(new_meta)

        # Create active layer record
        layer = GisLayer(
            dataset_id=new_dataset.id,
            layer_name=f"{new_dataset.name} (Raster)",
            layer_type="raster",
            feature_count=1,
            crs=raw_meta.get("crs")
        )
        db.add(layer)
        await db.commit()
        metadata_out = raw_meta

    await log_audit_event(
        db, action="DATASET_UPLOAD", user_id=current_user.id, user_email=current_user.email,
        entity_type="Dataset", entity_id=str(new_dataset.id),
        details={"filename": orig_filename, "size_bytes": file_size, "type": file_type},
        ip_address=request.client.host if request.client else None
    )

    return {
        "dataset_id": new_dataset.id,
        "name": new_dataset.name,
        "file_type": new_dataset.file_type,
        "file_size_bytes": new_dataset.file_size_bytes,
        "is_georeferenced": new_dataset.is_georeferenced,
        "warning": warning_msg,
        "metadata": metadata_out
    }

@router.get("")
async def list_datasets(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Dataset).order_by(Dataset.created_at.desc())
    result = await db.execute(stmt)
    datasets = result.scalars().all()
    return [
        {
            "id": d.id,
            "name": d.name,
            "filename": d.filename,
            "file_type": d.file_type,
            "file_size_bytes": d.file_size_bytes,
            "source_name": d.source_name,
            "source_url": d.source_url,
            "license": d.license,
            "is_georeferenced": d.is_georeferenced,
            "created_at": d.created_at
        }
        for d in datasets
    ]

@router.get("/{dataset_id}")
async def get_dataset_details(
    dataset_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Dataset).where(Dataset.id == dataset_id)
    result = await db.execute(stmt)
    dataset = result.scalars().first()
    if not dataset:
        raise HTTPException(status_code=404, detail="NO GIS DATA LOADED")

    meta_stmt = select(RasterMetadata).where(RasterMetadata.dataset_id == dataset_id)
    meta_res = await db.execute(meta_stmt)
    meta = meta_res.scalars().first()

    return {
        "id": dataset.id,
        "name": dataset.name,
        "filename": dataset.filename,
        "file_type": dataset.file_type,
        "file_size_bytes": dataset.file_size_bytes,
        "source_name": dataset.source_name,
        "source_url": dataset.source_url,
        "license": dataset.license,
        "is_georeferenced": dataset.is_georeferenced,
        "metadata": {
            "width": meta.width if meta else None,
            "height": meta.height if meta else None,
            "bands": meta.bands if meta else None,
            "data_type": meta.data_type if meta else None,
            "crs": meta.crs if (meta and meta.crs) else "CRS: Not Available",
            "gsd_cm": f"{meta.gsd_cm} cm/px" if (meta and meta.gsd_cm is not None) else "GSD: Not Available",
            "bounds": {
                "min_x": meta.bounds_min_x,
                "min_y": meta.bounds_min_y,
                "max_x": meta.bounds_max_x,
                "max_y": meta.bounds_max_y
            } if meta else None
        } if meta else "No metadata available"
    }

@router.delete("/{dataset_id}")
async def delete_dataset(
    dataset_id: int,
    request: Request,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    stmt = select(Dataset).where(Dataset.id == dataset_id)
    result = await db.execute(stmt)
    dataset = result.scalars().first()
    if not dataset:
        raise HTTPException(status_code=404, detail="Dataset not found")

    if os.path.exists(dataset.file_path):
        try:
            os.remove(dataset.file_path)
        except Exception:
            pass

    await db.delete(dataset)
    await db.commit()

    await log_audit_event(
        db, action="DATASET_DELETE", user_id=current_user.id, user_email=current_user.email,
        entity_type="Dataset", entity_id=str(dataset_id),
        ip_address=request.client.host if request.client else None
    )

    return {"message": f"Dataset {dataset_id} deleted successfully"}
