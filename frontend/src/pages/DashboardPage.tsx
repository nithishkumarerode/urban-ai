import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { DashboardStats } from '../types';
import { 
  Database, Layers, Home, Navigation, Trees, Waves, 
  Maximize2, GitCommit, Cpu, CheckCircle, AlertTriangle, RefreshCw
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/dashboard/statistics');
      setStats(response.data);
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Failed to connect to Cadastral GIS backend.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex items-center justify-center py-24">
          <RefreshCw className="h-8 w-8 text-cyan-400 animate-spin" />
          <span className="ml-3 text-slate-400">Loading Cadastral Intelligence Telemetry...</span>
        </div>
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400">
          <p className="font-semibold">Cadastral Database Offline</p>
          <p className="text-sm mt-1">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      {/* Header & Status Banner */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">
            UrbanCadastral AI Operations Center
          </h1>
          <p className="text-sm text-slate-400">
            Real-Time AI-Assisted Urban Cadastral Mapping & GIS Verification Platform
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* AI Model Status Badge */}
          <div className={`px-3 py-1.5 rounded-lg border text-xs font-semibold flex items-center gap-2 ${
            stats.ai_model_status === 'AVAILABLE'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
              : 'bg-amber-500/10 border-amber-500/30 text-amber-400'
          }`}>
            <Cpu className="h-4 w-4" />
            <span>{stats.ai_model_status}</span>
          </div>

          <button
            onClick={fetchStats}
            className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg border border-slate-700 transition-colors"
            title="Refresh Metrics"
          >
            <RefreshCw className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* No Dataset Warning Banner */}
      {!stats.has_data && (
        <div className="p-5 bg-slate-900 border border-amber-500/40 rounded-2xl flex items-start gap-4">
          <AlertTriangle className="h-6 w-6 text-amber-400 flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="text-base font-semibold text-white">
              No GIS dataset available.
            </h3>
            <p className="text-sm text-slate-400 mt-1">
              UrbanCadastral AI adheres strictly to ground-truth data integrity. No synthetic parcel polygons or simulated coordinates are loaded.
              Please upload a GeoTIFF orthomosaic or GeoJSON survey vector dataset via the Upload section to initiate AI extraction.
            </p>
            <div className="mt-3">
              <a
                href="/upload"
                className="inline-flex items-center px-3.5 py-1.5 bg-cyan-500 text-slate-950 font-semibold text-xs rounded-lg hover:bg-cyan-400 transition-colors"
              >
                Upload First Dataset
              </a>
            </div>
          </div>
        </div>
      )}

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Datasets */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-2xl shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Surveys & Rasters</span>
            <Database className="h-5 w-5 text-cyan-400" />
          </div>
          <div className="text-2xl font-bold text-white">{stats.total_datasets}</div>
          <div className="text-xs text-slate-400 mt-1">{stats.gis_dataset_status}</div>
        </div>

        {/* Total Parcels */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-2xl shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Parcels</span>
            <Layers className="h-5 w-5 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-white">{stats.total_parcels}</div>
          <div className="text-xs text-slate-400 mt-1">AI-derived / Verified boundaries</div>
        </div>

        {/* Buildings Detected */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-2xl shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Buildings Detected</span>
            <Home className="h-5 w-5 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold text-white">{stats.buildings_detected}</div>
          <div className="text-xs text-slate-400 mt-1">Extracted building footprints</div>
        </div>

        {/* Total Mapped Area */}
        <div className="p-5 bg-slate-900 border border-slate-800 rounded-2xl shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Mapped Extent</span>
            <Maximize2 className="h-5 w-5 text-purple-400" />
          </div>
          <div className="text-2xl font-bold text-white">
            {stats.total_mapped_area_hectares.toFixed(2)} <span className="text-sm font-normal text-slate-400">ha</span>
          </div>
          <div className="text-xs text-slate-400 mt-1">
            {stats.total_mapped_area_sqm.toLocaleString()} m² (Projected CRS)
          </div>
        </div>
      </div>

      {/* Secondary Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {/* Roads & Transport */}
        <div className="p-4 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 uppercase font-semibold">Road Corridors</div>
            <div className="text-xl font-bold text-white mt-1">{stats.roads_detected}</div>
          </div>
          <Navigation className="h-6 w-6 text-amber-400" />
        </div>

        {/* Vegetation & Environmental */}
        <div className="p-4 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 uppercase font-semibold">Vegetation Zones</div>
            <div className="text-xl font-bold text-white mt-1">{stats.vegetation_areas_count}</div>
          </div>
          <Trees className="h-6 w-6 text-emerald-400" />
        </div>

        {/* Water Bodies */}
        <div className="p-4 bg-slate-900/60 border border-slate-800 rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 uppercase font-semibold">Water Bodies</div>
            <div className="text-xl font-bold text-white mt-1">{stats.water_areas_count}</div>
          </div>
          <Waves className="h-6 w-6 text-cyan-400" />
        </div>
      </div>

      {/* Regulatory Cadastral Notice */}
      <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-500 leading-relaxed">
        <span className="font-semibold text-slate-400">Legal Boundary Notice:</span> This system provides AI-assisted preliminary cadastral mapping and validation. It automates feature detection, parcel proposals, and change analysis from georeferenced aerial and drone datasets. It does NOT claim to determine legal land ownership without statutory cadastral verification and ground truth survey seals.
      </div>
    </div>
  );
};
