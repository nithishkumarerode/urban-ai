import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Dataset } from '../types';
import { Upload, FileText, CheckCircle2, AlertCircle, AlertTriangle, Trash2, Globe } from 'lucide-react';

export const UploadPage: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [datasetName, setDatasetName] = useState('');
  const [sourceName, setSourceName] = useState('');
  const [sourceUrl, setSourceUrl] = useState('');
  const [license, setLicense] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [datasets, setDatasets] = useState<Dataset[]>([]);

  const fetchDatasets = async () => {
    try {
      const res = await api.get('/datasets');
      setDatasets(res.data);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    fetchDatasets();
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selected = e.target.files[0];
      setFile(selected);
      if (!datasetName) {
        setDatasetName(selected.name.replace(/\.[^/.]+$/, ''));
      }
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;

    setUploading(true);
    setError(null);
    setUploadResult(null);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('dataset_name', datasetName);
    if (sourceName) formData.append('source_name', sourceName);
    if (sourceUrl) formData.append('source_url', sourceUrl);
    if (license) formData.append('license', license);

    try {
      const res = await api.post('/datasets/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setUploadResult(res.data);
      setFile(null);
      setDatasetName('');
      fetchDatasets();
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Dataset upload and inspection failed.');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this dataset from the cadastre?')) return;
    try {
      await api.delete(`/datasets/${id}`);
      fetchDatasets();
    } catch (err) {
      alert('Failed to delete dataset.');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">
          Geospatial Ingestion & Raster Inspection
        </h1>
        <p className="text-sm text-slate-400">
          Upload real drone orthomosaics, GeoTIFF elevation models, or cadastral vector files.
        </p>
      </div>

      {/* Upload Box */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <form onSubmit={handleUpload} className="space-y-6">
          {error && (
            <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-center gap-3 text-rose-400 text-sm">
              <AlertCircle className="h-5 w-5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="border-2 border-dashed border-slate-700 hover:border-cyan-500/50 rounded-xl p-8 text-center transition-colors">
            <input
              type="file"
              id="file-upload"
              className="hidden"
              onChange={handleFileChange}
              accept=".tif,.tiff,.geojson,.json,.shp,.gpkg,.jpg,.jpeg,.png"
            />
            <label htmlFor="file-upload" className="cursor-pointer flex flex-col items-center">
              <div className="p-3 bg-cyan-500/10 rounded-full border border-cyan-500/20 mb-3">
                <Upload className="h-8 w-8 text-cyan-400" />
              </div>
              <span className="text-sm font-semibold text-white">
                {file ? file.name : 'Click to select GeoTIFF, GeoJSON, Shapefile or Image'}
              </span>
              <span className="text-xs text-slate-500 mt-1">
                Supported formats: .tif, .tiff, .geojson, .shp, .gpkg (Max size: 2GB)
              </span>
            </label>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Dataset Display Name
              </label>
              <input
                type="text"
                required
                value={datasetName}
                onChange={(e) => setDatasetName(e.target.value)}
                placeholder="e.g. Sector 4 Orthomosaic Flight #02"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Survey Provider / Organization
              </label>
              <input
                type="text"
                value={sourceName}
                onChange={(e) => setSourceName(e.target.value)}
                placeholder="e.g. Municipal Geodetic Survey Agency"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={!file || uploading}
            className="w-full py-2.5 px-4 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold rounded-lg text-sm transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {uploading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-slate-950" />
                <span>Extracting Geospatial Metadata & CRS...</span>
              </>
            ) : (
              <>
                <Upload className="h-4 w-4" />
                <span>Ingest & Validate Geospatial Metadata</span>
              </>
            )}
          </button>
        </form>
      </div>

      {/* Upload Result Metadata Display */}
      {uploadResult && (
        <div className="bg-slate-900 border border-cyan-500/30 rounded-2xl p-6 space-y-4">
          <div className="flex items-center gap-2 text-cyan-400 font-semibold text-sm">
            <CheckCircle2 className="h-5 w-5" />
            <span>Dataset Ingested & Inspected Successfully</span>
          </div>

          {uploadResult.warning && (
            <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-lg text-amber-400 text-xs flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 flex-shrink-0" />
              <span>{uploadResult.warning}</span>
            </div>
          )}

          {uploadResult.metadata && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-2">
              <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                <div className="text-xs text-slate-500">CRS (Coordinate System)</div>
                <div className="text-sm font-semibold text-white mt-1">
                  {uploadResult.metadata.crs || <span className="text-amber-400">CRS: Not Available</span>}
                </div>
              </div>

              <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                <div className="text-xs text-slate-500">Ground Resolution (GSD)</div>
                <div className="text-sm font-semibold text-white mt-1">
                  {uploadResult.metadata.gsd_cm ? `${uploadResult.metadata.gsd_cm} cm/px` : <span className="text-amber-400">GSD: Not Available</span>}
                </div>
              </div>

              <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                <div className="text-xs text-slate-500">Raster Dimensions</div>
                <div className="text-sm font-semibold text-white mt-1">
                  {uploadResult.metadata.width} × {uploadResult.metadata.height} px
                </div>
              </div>

              <div className="p-3 bg-slate-950 rounded-lg border border-slate-800">
                <div className="text-xs text-slate-500">Spectral Bands</div>
                <div className="text-sm font-semibold text-white mt-1">
                  {uploadResult.metadata.bands} Bands ({uploadResult.metadata.data_type})
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Dataset Inventory Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between">
          <h2 className="text-base font-semibold text-white flex items-center gap-2">
            <Globe className="h-5 w-5 text-cyan-400" />
            <span>Active Cadastral Survey Ingestion Log</span>
          </h2>
          <span className="text-xs text-slate-400">{datasets.length} records</span>
        </div>

        {datasets.length === 0 ? (
          <div className="p-8 text-center text-slate-500 text-sm">
            NO GIS DATA LOADED. Upload a survey dataset above to begin.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead className="bg-slate-950 text-xs uppercase text-slate-500 border-b border-slate-800">
                <tr>
                  <th className="px-6 py-3">Dataset Name</th>
                  <th className="px-6 py-3">File Type</th>
                  <th className="px-6 py-3">Size</th>
                  <th className="px-6 py-3">Source</th>
                  <th className="px-6 py-3">Ingestion Date</th>
                  <th className="px-6 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {datasets.map((d) => (
                  <tr key={d.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="px-6 py-4 font-semibold text-white">{d.name}</td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-0.5 rounded text-xs font-mono bg-slate-800 text-cyan-300 border border-slate-700">
                        {d.file_type}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs font-mono">
                      {(d.file_size_bytes / (1024 * 1024)).toFixed(2)} MB
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-400">{d.source_name || 'Direct Upload'}</td>
                    <td className="px-6 py-4 text-xs text-slate-500">
                      {new Date(d.created_at).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleDelete(d.id)}
                        className="p-1.5 text-slate-400 hover:text-rose-400 rounded transition-colors"
                        title="Delete Dataset"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
