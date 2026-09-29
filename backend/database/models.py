import uuid
from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text, BigInteger, JSON
)
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import relationship
from geoalchemy2 import Geometry
from .database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    uuid = Column(UUID(as_uuid=True), default=uuid.uuid4, unique=True)
    email = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    full_name = Column(String(255))
    role = Column(String(50), default="cadastral_analyst")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)
    updated_at = Column(DateTime(timezone=True), default=datetime.utcnow, onupdate=datetime.utcnow)

    datasets = relationship("Dataset", back_populates="uploader")

class Dataset(Base):
    __tablename__ = "datasets"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    filename = Column(String(255), nullable=False)
    file_type = Column(String(50), nullable=False)
    file_path = Column(String(512), nullable=False)
    file_size_bytes = Column(BigInteger, nullable=False)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    survey_date = Column(DateTime, nullable=True)
    source_name = Column(String(255), nullable=True)
    source_url = Column(String(512), nullable=True)
    license = Column(String(100), nullable=True)
    is_georeferenced = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    uploader = relationship("User", back_populates="datasets")
    metadata_rel = relationship("RasterMetadata", back_populates="dataset", uselist=False, cascade="all, delete-orphan")
    parcels = relationship("Parcel", back_populates="dataset", cascade="all, delete-orphan")
    buildings = relationship("Building", back_populates="dataset", cascade="all, delete-orphan")
    roads = relationship("Road", back_populates="dataset", cascade="all, delete-orphan")
    land_use = relationship("LandUse", back_populates="dataset", cascade="all, delete-orphan")
    layers = relationship("GisLayer", back_populates="dataset", cascade="all, delete-orphan")

class RasterMetadata(Base):
    __tablename__ = "raster_metadata"

    id = Column(Integer, primary_key=True, index=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), unique=True, nullable=False)
    width = Column(Integer, nullable=False)
    height = Column(Integer, nullable=False)
    bands = Column(Integer, nullable=False)
    data_type = Column(String(50), nullable=False)
    crs = Column(String(100), nullable=True) # None when unprojected / missing
    affine_transform = Column(JSONB, nullable=True)
    bounds_min_x = Column(Float, nullable=True)
    bounds_min_y = Column(Float, nullable=True)
    bounds_max_x = Column(Float, nullable=True)
    bounds_max_y = Column(Float, nullable=True)
    bounds_geom = Column(Geometry(geometry_type="POLYGON", srid=4326), nullable=True)
    resolution_x = Column(Float, nullable=True)
    resolution_y = Column(Float, nullable=True)
    gsd_cm = Column(Float, nullable=True) # None if unavailable
    nodata_value = Column(Float, nullable=True)
    inspection_timestamp = Column(DateTime(timezone=True), default=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="metadata_rel")

class GisLayer(Base):
    __tablename__ = "gis_layers"

    id = Column(Integer, primary_key=True, index=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    layer_name = Column(String(100), nullable=False)
    layer_type = Column(String(50), nullable=False)
    feature_count = Column(Integer, default=0)
    is_active = Column(Boolean, default=True)
    crs = Column(String(100), nullable=True)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="layers")

class Parcel(Base):
    __tablename__ = "parcels"

    id = Column(Integer, primary_key=True, index=True)
    parcel_identifier = Column(String(100), index=True, nullable=False)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="SET NULL"), nullable=True)
    geom = Column(Geometry(geometry_type="MULTIPOLYGON", srid=4326), nullable=False)
    projected_srid = Column(Integer, default=3857)
    area_sqm = Column(Float, nullable=False)
    area_hectares = Column(Float, nullable=False)
    perimeter_m = Column(Float, nullable=False)
    boundary_source = Column(String(100), default="AI-derived probable parcel boundary")
    ai_confidence = Column(Float, nullable=True)
    verification_status = Column(String(50), default="Pending") # Pending, Accepted, Rejected, Edited
    created_by = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)
    updated_at = Column(DateTime(timezone=True), default=datetime.utcnow, onupdate=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="parcels")
    buildings = relationship("Building", back_populates="parcel")

class Building(Base):
    __tablename__ = "buildings"

    id = Column(Integer, primary_key=True, index=True)
    building_code = Column(String(100), nullable=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="SET NULL"), nullable=True)
    parcel_id = Column(Integer, ForeignKey("parcels.id", ondelete="SET NULL"), nullable=True)
    geom = Column(Geometry(geometry_type="MULTIPOLYGON", srid=4326), nullable=False)
    area_sqm = Column(Float, nullable=False)
    estimated_height_m = Column(Float, nullable=True)
    ai_confidence = Column(Float, nullable=True)
    model_version = Column(String(50), nullable=True)
    verification_status = Column(String(50), default="Pending")
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="buildings")
    parcel = relationship("Parcel", back_populates="buildings")

class Road(Base):
    __tablename__ = "roads"

    id = Column(Integer, primary_key=True, index=True)
    road_identifier = Column(String(100), nullable=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="SET NULL"), nullable=True)
    geom = Column(Geometry(geometry_type="MULTILINESTRING", srid=4326), nullable=False)
    buffer_geom = Column(Geometry(geometry_type="MULTIPOLYGON", srid=4326), nullable=True)
    length_m = Column(Float, nullable=False)
    buffer_width_m = Column(Float, default=12.0)
    ai_confidence = Column(Float, nullable=True)
    verification_status = Column(String(50), default="Pending")
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="roads")

class LandUse(Base):
    __tablename__ = "land_use"

    id = Column(Integer, primary_key=True, index=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    class_code = Column(Integer, nullable=False) # 0=Background, 1=Building, 2=Road, 3=Veg, 4=Water, 5=Open
    class_name = Column(String(50), nullable=False)
    geom = Column(Geometry(geometry_type="MULTIPOLYGON", srid=4326), nullable=False)
    area_sqm = Column(Float, nullable=False)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

    dataset = relationship("Dataset", back_populates="land_use")

class AiPrediction(Base):
    __tablename__ = "ai_predictions"

    id = Column(Integer, primary_key=True, index=True)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    model_name = Column(String(100), nullable=False)
    model_version = Column(String(50), nullable=True)
    weights_path = Column(String(255), nullable=True)
    status = Column(String(50), nullable=False)
    total_tiles = Column(Integer, default=0)
    processed_tiles = Column(Integer, default=0)
    features_extracted_count = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
    started_at = Column(DateTime(timezone=True), default=datetime.utcnow)
    completed_at = Column(DateTime(timezone=True), nullable=True)

class VerificationTask(Base):
    __tablename__ = "verification_tasks"

    id = Column(Integer, primary_key=True, index=True)
    feature_type = Column(String(50), nullable=False)
    feature_id = Column(Integer, nullable=False)
    dataset_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    status = Column(String(50), default="Pending")
    assigned_user_id = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    original_geometry = Column(JSONB, nullable=True)
    edited_geometry = Column(JSONB, nullable=True)
    rejection_reason = Column(Text, nullable=True)
    notes = Column(Text, nullable=True)
    reviewed_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

class ChangeDetection(Base):
    __tablename__ = "change_detections"

    id = Column(Integer, primary_key=True, index=True)
    dataset_a_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    dataset_b_id = Column(Integer, ForeignKey("datasets.id", ondelete="CASCADE"), nullable=False)
    change_type = Column(String(100), nullable=False)
    description = Column(Text, nullable=False)
    geom = Column(Geometry(geometry_type="MULTIPOLYGON", srid=4326), nullable=True)
    area_shift_sqm = Column(Float, nullable=True)
    severity = Column(String(50), default="MODERATE")
    created_at = Column(DateTime(timezone=True), default=datetime.utcnow)

class AuditLog(Base):
    __tablename__ = "audit_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    user_email = Column(String(255), nullable=True)
    action = Column(String(100), nullable=False)
    entity_type = Column(String(50), nullable=True)
    entity_id = Column(String(100), nullable=True)
    details = Column(JSONB, nullable=True)
    ip_address = Column(String(45), nullable=True)
    timestamp = Column(DateTime(timezone=True), default=datetime.utcnow)
