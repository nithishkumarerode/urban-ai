export interface User {
  id: number;
  email: string;
  full_name?: string;
  role: string;
  is_active: boolean;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface Dataset {
  id: number;
  name: string;
  filename: string;
  file_type: string;
  file_size_bytes: number;
  source_name?: string;
  source_url?: string;
  license?: string;
  is_georeferenced: boolean;
  created_at: string;
  metadata?: RasterMetadata;
}

export interface RasterMetadata {
  width: number;
  height: number;
  bands: number;
  data_type: string;
  crs: string | null;
  gsd_cm: number | null;
  bounds?: {
    min_x: number;
    min_y: number;
    max_x: number;
    max_y: number;
  };
}

export interface Parcel {
  id: number;
  parcel_identifier: string;
  dataset_id?: number;
  area_sqm: number;
  area_hectares: number;
  perimeter_m: number;
  boundary_source: string;
  ai_confidence?: number;
  verification_status: 'Pending' | 'Accepted' | 'Rejected' | 'Edited';
  geometry: any;
}

export interface Building {
  id: number;
  building_code?: string;
  parcel_id?: number;
  area_sqm: number;
  estimated_height_m?: number;
  ai_confidence?: number;
  verification_status: string;
  geometry: any;
}

export interface DashboardStats {
  has_data: boolean;
  message: string;
  total_datasets: number;
  total_parcels: number;
  buildings_detected: number;
  roads_detected: number;
  vegetation_areas_count: number;
  water_areas_count: number;
  total_mapped_area_sqm: number;
  total_mapped_area_hectares: number;
  changes_detected: number;
  ai_model_status: string;
  ai_model_message: string;
  gis_dataset_status: string;
  last_processing_time: string | null;
}

export interface VerificationTask {
  id: number;
  feature_type: string;
  feature_id: number;
  dataset_id: number;
  status: string;
  original_geometry?: any;
  edited_geometry?: any;
  rejection_reason?: string;
  notes?: string;
  created_at: string;
}
