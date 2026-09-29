-- ==============================================================================
-- UrbanCadastral AI - PostGIS Database Schema
-- Real-Time AI-Assisted Urban Cadastral Mapping & GIS Platform
-- ==============================================================================

-- Enable PostGIS spatial extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users Table (Authentication & Role Management)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'cadastral_analyst', -- admin, cadastral_analyst, field_surveyor, auditor
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Datasets Table (Uploaded GeoTIFFs, Vectors, Surveys)
CREATE TABLE IF NOT EXISTS datasets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL, -- GeoTIFF, DSM, Orthomosaic, GeoJSON, Shapefile, GPKG
    file_path VARCHAR(512) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    survey_date DATE,
    source_name VARCHAR(255), -- e.g. "Government Geodetic Portal", "DJI P1 Flight #4"
    source_url VARCHAR(512),
    license VARCHAR(100),
    is_georeferenced BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Raster Metadata Table (Truthful GeoTIFF inspection)
CREATE TABLE IF NOT EXISTS raster_metadata (
    id SERIAL PRIMARY KEY,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE UNIQUE,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    bands INTEGER NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    crs VARCHAR(100), -- NULL if missing -> displayed as "CRS: Not Available"
    affine_transform JSONB,
    bounds_min_x DOUBLE PRECISION,
    bounds_min_y DOUBLE PRECISION,
    bounds_max_x DOUBLE PRECISION,
    bounds_max_y DOUBLE PRECISION,
    bounds_geom GEOMETRY(Polygon, 4326),
    resolution_x DOUBLE PRECISION,
    resolution_y DOUBLE PRECISION,
    gsd_cm DOUBLE PRECISION, -- NULL if cannot be calculated -> displayed as "GSD: Not Available"
    nodata_value DOUBLE PRECISION,
    inspection_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. GIS Layers Table (Layer inventory for Map display)
CREATE TABLE IF NOT EXISTS gis_layers (
    id SERIAL PRIMARY KEY,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE,
    layer_name VARCHAR(100) NOT NULL,
    layer_type VARCHAR(50) NOT NULL, -- raster, parcel, building, road, land_use, change_detection
    feature_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    crs VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Parcels Table (AI-derived probable parcel boundaries & user-verified parcels)
CREATE TABLE IF NOT EXISTS parcels (
    id SERIAL PRIMARY KEY,
    parcel_identifier VARCHAR(100) NOT NULL,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE SET NULL,
    geom GEOMETRY(MultiPolygon, 4326) NOT NULL,
    projected_srid INTEGER DEFAULT 3857,
    area_sqm DOUBLE PRECISION NOT NULL,
    area_hectares DOUBLE PRECISION NOT NULL,
    perimeter_m DOUBLE PRECISION NOT NULL,
    boundary_source VARCHAR(100) DEFAULT 'AI-derived probable parcel boundary', -- 'AI-derived probable parcel boundary', 'User-drawn geometry', 'Official Cadastral Registry'
    ai_confidence DOUBLE PRECISION,
    verification_status VARCHAR(50) DEFAULT 'Pending', -- Pending, Accepted, Rejected, Edited
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Buildings Table (Extracted Footprints)
CREATE TABLE IF NOT EXISTS buildings (
    id SERIAL PRIMARY KEY,
    building_code VARCHAR(100),
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE SET NULL,
    parcel_id INTEGER REFERENCES parcels(id) ON DELETE SET NULL,
    geom GEOMETRY(MultiPolygon, 4326) NOT NULL,
    area_sqm DOUBLE PRECISION NOT NULL,
    estimated_height_m DOUBLE PRECISION,
    ai_confidence DOUBLE PRECISION,
    model_version VARCHAR(50),
    verification_status VARCHAR(50) DEFAULT 'Pending',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Roads Table (Extracted Road corridors and centerlines)
CREATE TABLE IF NOT EXISTS roads (
    id SERIAL PRIMARY KEY,
    road_identifier VARCHAR(100),
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE SET NULL,
    geom GEOMETRY(MultiLineString, 4326) NOT NULL,
    buffer_geom GEOMETRY(MultiPolygon, 4326),
    length_m DOUBLE PRECISION NOT NULL,
    buffer_width_m DOUBLE PRECISION DEFAULT 12.0,
    ai_confidence DOUBLE PRECISION,
    verification_status VARCHAR(50) DEFAULT 'Pending',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Land Use Table (Automated multi-class segmentation)
CREATE TABLE IF NOT EXISTS land_use (
    id SERIAL PRIMARY KEY,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE,
    class_code INTEGER NOT NULL, -- 0=Background, 1=Building, 2=Road, 3=Vegetation, 4=Water, 5=Open Land
    class_name VARCHAR(50) NOT NULL,
    geom GEOMETRY(MultiPolygon, 4326) NOT NULL,
    area_sqm DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. AI Predictions / Pipeline Jobs
CREATE TABLE IF NOT EXISTS ai_predictions (
    id SERIAL PRIMARY KEY,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(50),
    weights_path VARCHAR(255),
    status VARCHAR(50) NOT NULL, -- Upload received, Reading raster, Checking CRS, Preparing tiles, AI inference, Polygonization, GIS processing, Completed, Failed
    total_tiles INTEGER DEFAULT 0,
    processed_tiles INTEGER DEFAULT 0,
    features_extracted_count INTEGER DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

-- 10. Verification Tasks Table (Human-in-the-loop workflow)
CREATE TABLE IF NOT EXISTS verification_tasks (
    id SERIAL PRIMARY KEY,
    feature_type VARCHAR(50) NOT NULL, -- parcel, building, road
    feature_id INTEGER NOT NULL,
    dataset_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'Pending', -- Pending, Accepted, Rejected, Edited
    assigned_user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    original_geometry JSONB,
    edited_geometry JSONB,
    rejection_reason TEXT,
    notes TEXT,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Change Detections Table (Temporal survey comparison)
CREATE TABLE IF NOT EXISTS change_detections (
    id SERIAL PRIMARY KEY,
    dataset_a_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE, -- baseline survey
    dataset_b_id INTEGER REFERENCES datasets(id) ON DELETE CASCADE, -- current survey
    change_type VARCHAR(100) NOT NULL, -- New Building, Removed Building, Boundary Shift, Road Encroachment, Land Use Shift
    description TEXT NOT NULL,
    geom GEOMETRY(MultiPolygon, 4326),
    area_shift_sqm DOUBLE PRECISION,
    severity VARCHAR(50) DEFAULT 'MODERATE', -- LOW, MODERATE, CRITICAL
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. Audit Logs Table (Immutable regulatory audit trail)
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    user_email VARCHAR(255),
    action VARCHAR(100) NOT NULL, -- LOGIN, DATASET_UPLOAD, AI_PROCESSING, PARCEL_CREATE, PARCEL_EDIT, VERIFICATION, EXPORT, DATASET_DELETE
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    details JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- PostGIS Spatial Indices
CREATE INDEX IF NOT EXISTS idx_parcels_geom ON parcels USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_buildings_geom ON buildings USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_roads_geom ON roads USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_land_use_geom ON land_use USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_raster_bounds_geom ON raster_metadata USING GIST (bounds_geom);
CREATE INDEX IF NOT EXISTS idx_change_detections_geom ON change_detections USING GIST (geom);
