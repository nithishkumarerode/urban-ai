import React, { useEffect, useState, useRef } from 'react';
import { api } from '../services/api';
import { Parcel, Building } from '../types';
import { 
  Layers, Search, PlusCircle, Check, X, Trash2, Cpu, 
  AlertTriangle, Navigation, MapPin, MousePointer, ShieldCheck 
} from 'lucide-react';
import L from 'leaflet';

export const MapPage: React.FC = () => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const parcelLayerGroupRef = useRef<L.FeatureGroup | null>(null);

  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedParcel, setSelectedParcel] = useState<Parcel | null>(null);
  const [linkedBuildings, setLinkedBuildings] = useState<Building[]>([]);
  const [cursorCoords, setCursorCoords] = useState<{ lat: number; lng: number } | null>(null);

  // Editor states
  const [isDrawing, setIsDrawing] = useState(false);
  const [drawnVertices, setDrawnVertices] = useState<[number, number][]>([]);
  const [newParcelId, setNewParcelId] = useState('');
  const [editorMsg, setEditorMsg] = useState<string | null>(null);

  // Layer toggles
  const [showParcels, setShowParcels] = useState(true);
  const [showBuildings, setShowBuildings] = useState(true);

  // Search state
  const [searchQuery, setSearchQuery] = useState('');

  // AI Pipeline trigger
  const [aiStatus, setAiStatus] = useState<string | null>(null);

  // Fetch initial data
  const fetchData = async () => {
    try {
      const [parcelsRes, bldgRes] = await Promise.all([
        api.get('/gis/parcels'),
        api.get('/gis/buildings'),
      ]);
      setParcels(parcelsRes.data);
      setBuildings(bldgRes.data);
    } catch (e) {
      // ignore
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Initialize Leaflet Map
  useEffect(() => {
    if (!mapContainerRef.current || mapInstanceRef.current) return;

    const map = L.map(mapContainerRef.current, {
      center: [52.5200, 13.4050],
      zoom: 16,
      zoomControl: true,
    });

    // Dark Basemap tile layer
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://carto.com/">CARTO</a> &copy; OpenStreetMap contributors',
      maxZoom: 20,
    }).addTo(map);

    // Scale bar
    L.control.scale({ imperial: false, metric: true }).addTo(map);

    // Feature group for parcels
    const parcelGroup = L.featureGroup().addTo(map);
    parcelLayerGroupRef.current = parcelGroup;

    // Track cursor coordinates
    map.on('mousemove', (e) => {
      setCursorCoords({ lat: e.latlng.lat, lng: e.latlng.lng });
    });

    // Click handler for drawing
    map.on('click', (e) => {
      if ((window as any).__isDrawingActive) {
        (window as any).__addVertex(e.latlng.lat, e.latlng.lng);
      }
    });

    mapInstanceRef.current = map;

    return () => {
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // Keep drawing active ref in sync
  useEffect(() => {
    (window as any).__isDrawingActive = isDrawing;
    (window as any).__addVertex = (lat: number, lng: number) => {
      setDrawnVertices((prev) => [...prev, [lat, lng]]);
    };
  }, [isDrawing]);

  // Render Parcels on Map
  useEffect(() => {
    if (!mapInstanceRef.current || !parcelLayerGroupRef.current) return;
    parcelLayerGroupRef.current.clearLayers();

    if (showParcels) {
      parcels.forEach((p) => {
        if (p.geometry && p.geometry.coordinates) {
          const coords = p.geometry.coordinates;
          // Coordinates in GeoJSON are [lng, lat], Leaflet uses [lat, lng]
          const latlngs = coords[0][0].map((pt: [number, number]) => [pt[1], pt[0]]);
          
          const polygon = L.polygon(latlngs, {
            color: p.verification_status === 'Accepted' ? '#10b981' : p.verification_status === 'Rejected' ? '#ef4444' : '#00f2fe',
            weight: selectedParcel?.id === p.id ? 3 : 1.5,
            fillOpacity: selectedParcel?.id === p.id ? 0.35 : 0.15,
          });

          polygon.on('click', () => {
            setSelectedParcel(p);
            // Find linked buildings
            const linked = buildings.filter((b) => b.parcel_id === p.id);
            setLinkedBuildings(linked);
          });

          parcelLayerGroupRef.current?.addLayer(polygon);
        }
      });
    }

    // Render active drawing vertices
    if (drawnVertices.length > 0) {
      const drawPoly = L.polyline(drawnVertices, { color: '#f59e0b', dashArray: '4, 4' });
      parcelLayerGroupRef.current.addLayer(drawPoly);
      drawnVertices.forEach((v) => {
        const marker = L.circleMarker(v, { radius: 5, color: '#f59e0b', fillOpacity: 1 });
        parcelLayerGroupRef.current?.addLayer(marker);
      });
    }
  }, [parcels, showParcels, selectedParcel, drawnVertices, buildings]);

  const handleSaveDrawnParcel = async () => {
    if (drawnVertices.length < 3) {
      setEditorMsg('A parcel polygon requires at least 3 vertices.');
      return;
    }

    const parcelIdToUse = newParcelId.trim() || `PARCEL-${Date.now().toString().slice(-4)}`;
    
    // Close polygon
    const closed = [...drawnVertices, drawnVertices[0]];
    // Convert to GeoJSON [lng, lat]
    const geojsonCoords = [closed.map((v) => [v[1], v[0]])];

    try {
      const payload = {
        parcel_identifier: parcelIdToUse,
        geometry: {
          type: 'Polygon',
          coordinates: geojsonCoords,
        },
        source: 'User-drawn geometry',
      };

      const res = await api.post('/gis/parcels', payload);
      setEditorMsg(`Parcel ${res.data.parcel_identifier} saved (${res.data.area_sqm} m²).`);
      setIsDrawing(false);
      setDrawnVertices([]);
      setNewParcelId('');
      fetchData();
    } catch (err: any) {
      setEditorMsg(err.response?.data?.detail || 'Failed to save parcel geometry.');
    }
  };

  const handleCheckAdjacent = async () => {
    try {
      const res = await api.get('/gis/adjacent-check');
      setEditorMsg(res.data.message);
    } catch (err) {
      setEditorMsg('Check failed.');
    }
  };

  const handleDeleteParcel = async (id: number) => {
    if (!confirm(`Delete parcel #${id}?`)) return;
    try {
      await api.delete(`/gis/parcels/${id}`);
      setSelectedParcel(null);
      fetchData();
    } catch (err) {
      alert('Failed to delete parcel.');
    }
  };

  const handleRunAiScan = async () => {
    setAiStatus('Connecting to AI inference pipeline...');
    try {
      const res = await api.post('/ai/scan/1');
      setAiStatus('Inference complete.');
    } catch (err: any) {
      // Strictly shows AI MODEL NOT AVAILABLE
      setAiStatus(err.response?.data?.detail || 'AI MODEL NOT AVAILABLE');
    }
  };

  return (
    <div className="h-[calc(100vh-4rem)] flex flex-col md:flex-row overflow-hidden bg-slate-950">
      {/* Sidebar Controls */}
      <div className="w-full md:w-96 bg-slate-900 border-r border-slate-800 flex flex-col overflow-y-auto">
        <div className="p-4 border-b border-slate-800 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
              <Layers className="h-4 w-4 text-cyan-400" />
              <span>Cadastral GIS Engine</span>
            </h2>
            <button
              onClick={handleRunAiScan}
              className="px-2.5 py-1 bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 rounded text-xs font-semibold flex items-center gap-1.5 transition-colors"
              title="Run AI Extraction"
            >
              <Cpu className="h-3.5 w-3.5" />
              <span>AI Scan</span>
            </button>
          </div>

          {/* AI Status Alert */}
          {aiStatus && (
            <div className={`p-2.5 rounded text-xs font-medium ${
              aiStatus.includes('NOT AVAILABLE')
                ? 'bg-amber-500/10 border border-amber-500/30 text-amber-300'
                : 'bg-cyan-500/10 border border-cyan-500/30 text-cyan-300'
            }`}>
              {aiStatus}
            </div>
          )}

          {/* Search */}
          <div className="relative">
            <Search className="h-4 w-4 absolute left-3 top-2.5 text-slate-500" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search parcel ID or coordinates..."
              className="w-full pl-9 pr-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
            />
          </div>
        </div>

        {/* Vector GIS Editor Controls */}
        <div className="p-4 border-b border-slate-800 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
              Vector Parcel Editor
            </span>
            <span className="text-xs text-slate-500">{parcels.length} parcels in DB</span>
          </div>

          {!isDrawing ? (
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => {
                  setIsDrawing(true);
                  setDrawnVertices([]);
                  setEditorMsg('Click on map to add boundary vertices.');
                }}
                className="py-2 px-3 bg-cyan-500/10 border border-cyan-500/30 hover:bg-cyan-500/20 text-cyan-400 rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
              >
                <PlusCircle className="h-4 w-4" />
                <span>Draw Parcel</span>
              </button>

              <button
                onClick={handleCheckAdjacent}
                className="py-2 px-3 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-lg text-xs font-semibold transition-colors"
              >
                Create Adjacent
              </button>
            </div>
          ) : (
            <div className="space-y-2 p-3 bg-slate-950 rounded-lg border border-amber-500/40">
              <div className="text-xs text-amber-300 font-semibold flex items-center justify-between">
                <span>Drawing in progress...</span>
                <span>{drawnVertices.length} pts</span>
              </div>
              <input
                type="text"
                value={newParcelId}
                onChange={(e) => setNewParcelId(e.target.value)}
                placeholder="Parcel Identifier (e.g. P-402)"
                className="w-full px-2 py-1 bg-slate-900 border border-slate-700 rounded text-xs text-white"
              />
              <div className="flex gap-2 pt-1">
                <button
                  onClick={handleSaveDrawnParcel}
                  className="flex-1 py-1 px-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded text-xs flex items-center justify-center gap-1"
                >
                  <Check className="h-3.5 w-3.5" />
                  <span>Save Geometry</span>
                </button>
                <button
                  onClick={() => {
                    setIsDrawing(false);
                    setDrawnVertices([]);
                    setEditorMsg(null);
                  }}
                  className="py-1 px-2 bg-slate-800 hover:bg-slate-700 text-slate-400 rounded text-xs"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
          )}

          {editorMsg && (
            <div className="p-2 bg-slate-950 rounded text-xs text-cyan-300 border border-slate-800">
              {editorMsg}
            </div>
          )}
        </div>

        {/* Selected Feature Inspector */}
        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          <div className="text-xs font-bold text-slate-300 uppercase tracking-wider">
            Cadastral Feature Inspector
          </div>

          {selectedParcel ? (
            <div className="bg-slate-950 rounded-xl p-4 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                <span className="font-bold text-white text-sm">
                  {selectedParcel.parcel_identifier}
                </span>
                <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                  selectedParcel.verification_status === 'Accepted'
                    ? 'bg-emerald-500/20 text-emerald-400'
                    : selectedParcel.verification_status === 'Rejected'
                    ? 'bg-rose-500/20 text-rose-400'
                    : 'bg-amber-500/20 text-amber-400'
                }`}>
                  {selectedParcel.verification_status}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="text-slate-500">Projected Area:</div>
                <div className="text-slate-200 font-mono text-right">
                  {selectedParcel.area_sqm} m²
                </div>

                <div className="text-slate-500">Hectares:</div>
                <div className="text-slate-200 font-mono text-right">
                  {selectedParcel.area_hectares} ha
                </div>

                <div className="text-slate-500">Perimeter:</div>
                <div className="text-slate-200 font-mono text-right">
                  {selectedParcel.perimeter_m} m
                </div>

                <div className="text-slate-500">Boundary Source:</div>
                <div className="text-slate-300 text-right text-xs truncate">
                  {selectedParcel.boundary_source}
                </div>
              </div>

              {/* Spatial Intersection - Buildings inside Parcel */}
              <div className="border-t border-slate-800 pt-3">
                <div className="text-xs font-semibold text-slate-400 mb-2">
                  Spatial Containment:
                </div>
                {linkedBuildings.length === 0 ? (
                  <div className="text-xs text-slate-500 italic">
                    No building linked.
                  </div>
                ) : (
                  <div className="space-y-1.5">
                    {linkedBuildings.map((b) => (
                      <div key={b.id} className="p-2 bg-slate-900 rounded border border-slate-800 text-xs flex justify-between items-center">
                        <span className="text-white font-mono">{b.building_code || `B-${b.id}`}</span>
                        <span className="text-emerald-400 font-mono">{b.area_sqm} m²</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-2">
                <button
                  onClick={() => handleDeleteParcel(selectedParcel.id)}
                  className="w-full py-1.5 px-3 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded text-xs font-semibold transition-colors flex items-center justify-center gap-1.5"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                  <span>Delete Parcel</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="p-6 text-center text-slate-500 text-xs border border-dashed border-slate-800 rounded-xl">
              Click on any parcel boundary on the GIS map or draw a new parcel above to view spatial attributes.
            </div>
          )}
        </div>
      </div>

      {/* Main Leaflet Map Area */}
      <div className="flex-1 relative h-full">
        {/* Empty State Banner if 0 parcels and no dataset */}
        {parcels.length === 0 && (
          <div className="absolute top-4 left-4 z-[1000] bg-slate-900/90 backdrop-blur border border-cyan-500/40 px-4 py-2 rounded-xl text-xs text-slate-300 flex items-center gap-2 shadow-xl">
            <AlertTriangle className="h-4 w-4 text-cyan-400 flex-shrink-0" />
            <span>NO GIS DATA LOADED. Click "Draw Parcel" on left to begin.</span>
          </div>
        )}

        {/* Real-time Cursor Coordinates Bar */}
        <div className="absolute bottom-4 right-4 z-[1000] bg-slate-900/90 backdrop-blur border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-mono text-slate-300 flex items-center gap-2 shadow-lg">
          <Navigation className="h-3.5 w-3.5 text-cyan-400" />
          <span>
            {cursorCoords
              ? `Lat: ${cursorCoords.lat.toFixed(6)}°, Lng: ${cursorCoords.lng.toFixed(6)}°`
              : 'Cursor: Off-map'}
          </span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-400">CRS: EPSG:4326 / EPSG:3857</span>
        </div>

        {/* Leaflet Map Div */}
        <div ref={mapContainerRef} className="w-full h-full" />
      </div>
    </div>
  );
};
