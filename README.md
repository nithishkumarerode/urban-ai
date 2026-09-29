# UrbanCadastral AI – Real-Time AI-Assisted Urban Cadastral Mapping & GIS Platform

A professional government-grade, production-style cadastral mapping and geospatial intelligence platform.

> **LEGAL NOTICE & GROUND TRUTH POLICY:**
> This system provides AI-assisted preliminary cadastral mapping and validation. It does **NOT** claim to determine legal land ownership from aerial imagery.
> Every displayed metric, parcel boundary, and coordinate originates from uploaded real data, the database, or the AI pipeline. If data is absent, the system displays **"NO GIS DATA LOADED"** or **"AI MODEL NOT AVAILABLE"** rather than inventing synthetic coordinates.

---

## 1. System Architecture

- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, Leaflet GIS, Lucide icons, Recharts
- **Backend**: Python 3.11, FastAPI, Uvicorn, Pydantic v2
- **Spatial GIS Engine**: GDAL, Rasterio, Shapely, PyProj, GeoPandas, GeoAlchemy2
- **Database**: PostgreSQL 16 with PostGIS 3.4 Spatial Extension
- **AI Inference Engine**: ONNX Runtime, OpenCV, PyTorch (Tiled segmentation on demand)
- **Security**: JWT Authentication (HS256), Bcrypt password hashing, Role-Based Access Control, Immutable Regulatory Audit Logging

---

## 2. Directory Structure

```
UrbanCadastral/
├── frontend/                     # React + TypeScript + Vite + Tailwind + Leaflet
│   ├── src/
│   │   ├── components/           # Navbar, ProtectedRoute
│   │   ├── pages/                # Login, Dashboard, Upload, Map, Verification, ChangeDetection
│   │   ├── services/             # Axios API client with JWT interceptor
│   │   ├── context/              # AuthContext session management
│   │   └── types/                # Strict TypeScript models
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
│
├── backend/                      # Python FastAPI application
│   ├── main.py                   # FastAPI entrypoint
│   ├── api/                      # REST routers: auth, datasets, ai, gis, verification, change, reports
│   ├── database/                 # SQLAlchemy async engine, models, seed scripts
│   ├── gis/                      # Raster inspection & projected metric area calculations
│   ├── ai/                       # ONNX inference pipeline & weights validator
│   ├── services/                 # Regulatory audit logging service
│   ├── models/weights/           # Real AI model weights directory
│   ├── requirements.txt
│   └── Dockerfile
│
├── database/
│   └── schema.sql                # PostGIS spatial DDL & tables
│
├── data/
│   ├── uploads/                  # Ingested GeoTIFFs, GeoJSONs, Shapefiles
│   ├── processed/                # Tiled rasters and segmentation outputs
│   └── exports/                  # Exported GeoJSON / CSV cadastre reports
│
├── .env.example                  # Environment configuration template
├── docker-compose.yml            # Complete multi-container orchestration
└── README.md
```

---

## 3. Quickstart with Docker Compose

1. **Clone the repository and prepare environment**:
   ```bash
   cp .env.example .env
   ```

2. **Launch all services**:
   ```bash
   docker-compose up -d --build
   ```

3. **Access the application**:
   - **Frontend GIS Web App**: `http://localhost:5173`
   - **Backend API & Swagger Docs**: `http://localhost:8000/docs`
   - **PostGIS Database**: `localhost:5432` (`urbancadastral_db`)

4. **Default Development Test Credentials**:
   - **Email**: `test@example.com`
   - **Password**: `AdminCadastral2026!`

---

## 4. Key Endpoints

| Category | Endpoint | Method | Description |
|---|---|---|---|
| **Auth** | `/api/auth/login` | POST | Authenticates user and issues signed JWT bearer token |
| **Datasets** | `/api/datasets/upload` | POST | Ingests GeoTIFF / GeoJSON, inspects CRS, GSD, bands, dimensions |
| **Datasets** | `/api/datasets` | GET | Lists all ingested datasets |
| **GIS** | `/api/gis/parcels` | GET | Returns parcels in GeoJSON format |
| **GIS** | `/api/gis/parcels` | POST | Creates parcel from drawn vertices, calculates projected metric area |
| **GIS** | `/api/gis/adjacent-check` | GET | Validates if adjacent parcel creation is allowed |
| **AI** | `/api/ai/scan/{dataset_id}` | POST | Runs ONNX inference if weights exist, otherwise returns 503 |
| **Verification** | `/api/verification` | GET | Review queue of AI-derived features |
| **Verification** | `/api/verification/{id}/accept` | POST | Cadastral analyst acceptance |
| **Changes** | `/api/change-detection` | POST | Compares two dated survey datasets |
| **Reports** | `/api/reports/geojson/{id}` | GET | Exports official GeoJSON boundary file |
| **Reports** | `/api/reports/summary/{id}` | GET | Generates complete preliminary cadastral metrics report |
