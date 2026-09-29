import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Dataset } from '../types';
import { GitCompare, AlertTriangle, ArrowRight, ShieldCheck } from 'lucide-react';

export const ChangeDetectionPage: React.FC = () => {
  const [datasets, setDatasets] = useState<Dataset[]>([]);
  const [selectedA, setSelectedA] = useState<string>('');
  const [selectedB, setSelectedB] = useState<string>('');
  const [resultMsg, setResultMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [comparing, setComparing] = useState(false);

  useEffect(() => {
    const fetchDs = async () => {
      try {
        const res = await api.get('/datasets');
        setDatasets(res.data);
        if (res.data.length >= 2) {
          setSelectedA(res.data[0].id.toString());
          setSelectedB(res.data[1].id.toString());
        }
      } catch (e) {
        // ignore
      }
    };
    fetchDs();
  }, []);

  const handleRunComparison = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setResultMsg(null);
    setComparing(true);

    try {
      const res = await api.post('/change-detection', {
        dataset_a_id: parseInt(selectedA),
        dataset_b_id: parseInt(selectedB),
      });
      setResultMsg(res.data.message);
    } catch (err: any) {
      setErrorMsg(
        err.response?.data?.detail || 'Second dated dataset required for temporal change detection.'
      );
    } finally {
      setComparing(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">
          Temporal Cadastral Change Detection
        </h1>
        <p className="text-sm text-slate-400">
          Compare multi-temporal geodetic surveys to detect encroaching boundaries and footprint shifts.
        </p>
      </div>

      {datasets.length < 2 && (
        <div className="p-5 bg-slate-900 border border-amber-500/40 rounded-2xl flex items-start gap-4">
          <AlertTriangle className="h-6 w-6 text-amber-400 flex-shrink-0 mt-0.5" />
          <div>
            <h3 className="text-base font-semibold text-white">
              Second dated dataset required for temporal change detection.
            </h3>
            <p className="text-sm text-slate-400 mt-1">
              Currently, fewer than two georeferenced surveys exist in the database ({datasets.length} loaded).
              UrbanCadastral AI does not invent synthetic baseline surveys. Please upload a prior dated survey dataset to enable differential spatial analysis.
            </p>
          </div>
        </div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
        <form onSubmit={handleRunComparison} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            {/* Survey A */}
            <div className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Dataset A: Baseline / Previous Survey
              </label>
              <select
                value={selectedA}
                onChange={(e) => setSelectedA(e.target.value)}
                disabled={datasets.length < 2}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 disabled:opacity-50"
              >
                {datasets.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.file_type})
                  </option>
                ))}
              </select>
            </div>

            {/* Survey B */}
            <div className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Dataset B: Current / Recent Survey
              </label>
              <select
                value={selectedB}
                onChange={(e) => setSelectedB(e.target.value)}
                disabled={datasets.length < 2}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-cyan-500 disabled:opacity-50"
              >
                {datasets.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.file_type})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={datasets.length < 2 || comparing}
            className="w-full py-2.5 px-4 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold rounded-lg text-sm transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <GitCompare className="h-4 w-4" />
            <span>Execute Differential Cadastral Comparison</span>
          </button>
        </form>

        {errorMsg && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400 text-sm">
            {errorMsg}
          </div>
        )}

        {resultMsg && (
          <div className="p-4 bg-cyan-500/10 border border-cyan-500/30 rounded-xl text-cyan-300 text-sm">
            {resultMsg}
          </div>
        )}
      </div>
    </div>
  );
};
