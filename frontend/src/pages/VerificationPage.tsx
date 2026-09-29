import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Parcel } from '../types';
import { CheckCircle2, XCircle, Edit3, ShieldAlert, Clock, UserCheck } from 'lucide-react';

export const VerificationPage: React.FC = () => {
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionMsg, setActionMsg] = useState<string | null>(null);

  const fetchVerificationQueue = async () => {
    setLoading(true);
    try {
      const res = await api.get('/gis/parcels');
      setParcels(res.data);
    } catch (e) {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVerificationQueue();
  }, []);

  const handleUpdateStatus = async (parcelId: number, newStatus: 'Accepted' | 'Rejected') => {
    try {
      await api.put(`/gis/parcels/${parcelId}`, { verification_status: newStatus });
      setActionMsg(`Parcel #${parcelId} status set to ${newStatus}.`);
      fetchVerificationQueue();
    } catch (err: any) {
      setActionMsg('Failed to update status.');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">
            Human-in-the-Loop Verification Queue
          </h1>
          <p className="text-sm text-slate-400">
            Official cadastral review of AI-extracted parcel boundaries and survey vectors.
          </p>
        </div>
      </div>

      {actionMsg && (
        <div className="p-3 bg-cyan-500/10 border border-cyan-500/30 rounded-xl text-cyan-300 text-xs flex items-center gap-2">
          <UserCheck className="h-4 w-4" />
          <span>{actionMsg}</span>
        </div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between">
          <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
            Review Queue Items
          </span>
          <span className="text-xs text-slate-500">{parcels.length} total parcels</span>
        </div>

        {parcels.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-sm">
            NO GIS DATA LOADED. No pending cadastral features requiring review.
          </div>
        ) : (
          <div className="divide-y divide-slate-800">
            {parcels.map((p) => (
              <div key={p.id} className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-800/30 transition-colors">
                <div className="space-y-1">
                  <div className="flex items-center gap-3">
                    <span className="font-bold text-white text-base">{p.parcel_identifier}</span>
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                      p.verification_status === 'Accepted'
                        ? 'bg-emerald-500/20 text-emerald-400'
                        : p.verification_status === 'Rejected'
                        ? 'bg-rose-500/20 text-rose-400'
                        : 'bg-amber-500/20 text-amber-400'
                    }`}>
                      {p.verification_status}
                    </span>
                  </div>
                  <div className="text-xs text-slate-400 flex items-center gap-4 pt-1 font-mono">
                    <span>Area: {p.area_sqm} m² ({p.area_hectares} ha)</span>
                    <span>Perimeter: {p.perimeter_m} m</span>
                    <span className="text-slate-500 truncate max-w-xs">{p.boundary_source}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleUpdateStatus(p.id, 'Accepted')}
                    className="px-3 py-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors"
                  >
                    <CheckCircle2 className="h-4 w-4" />
                    <span>Accept</span>
                  </button>

                  <button
                    onClick={() => handleUpdateStatus(p.id, 'Rejected')}
                    className="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors"
                  >
                    <XCircle className="h-4 w-4" />
                    <span>Reject</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
