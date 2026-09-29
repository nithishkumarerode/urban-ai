import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from backend.api import auth, datasets, ai, gis, verification, change_detection, dashboard, search, reports

app = FastAPI(
    title="UrbanCadastral AI API",
    description="Real-Time AI-Assisted Urban Cadastral Mapping & GIS Platform Backend",
    version="1.0.0"
)

# CORS configuration
origins = [
    "http://localhost:3000",
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "*"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount all API routers
app.include_router(auth.router, prefix="/api")
app.include_router(datasets.router, prefix="/api")
app.include_router(ai.router, prefix="/api")
app.include_router(gis.router, prefix="/api")
app.include_router(verification.router, prefix="/api")
app.include_router(change_detection.router, prefix="/api")
app.include_router(dashboard.router, prefix="/api")
app.include_router(search.router, prefix="/api")
app.include_router(reports.router, prefix="/api")

@app.get("/")
async def root():
    return {
        "platform": "UrbanCadastral AI",
        "description": "AI-assisted preliminary cadastral mapping and validation",
        "status": "ONLINE",
        "notice": "This system does NOT claim to determine legal land ownership from aerial imagery."
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}
