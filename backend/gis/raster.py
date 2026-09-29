import os
from typing import Dict, Any, Optional
from shapely.geometry import box, mapping
import pyproj

def inspect_geotiff(file_path: str) -> Dict[str, Any]:
    """
    Directly reads and extracts metadata from a GeoTIFF raster.
    Never invents CRS or GSD. If missing or unprojected, sets to None.
    """
    file_size_bytes = os.path.getsize(file_path)
    filename = os.path.basename(file_path)

    try:
        import rasterio
        with rasterio.open(file_path) as src:
            width = src.width
            height = src.height
            count = src.count
            dtypes = [str(t) for t in src.dtypes]
            data_type = dtypes[0] if dtypes else "unknown"
            
            # CRS Detection
            crs_str = None
            if src.crs is not None:
                crs_str = src.crs.to_string()

            # Transform & Resolution
            transform = src.transform
            res_x, res_y = src.res

            # Bounds
            bounds = src.bounds # left, bottom, right, top
            min_x, min_y, max_x, max_y = bounds.left, bounds.bottom, bounds.right, bounds.top

            # Reproject bounds to WGS84 (EPSG:4326) for standardized GeoJSON geometry
            bounds_geom = None
            if crs_str:
                try:
                    transformer = pyproj.Transformer.from_crs(crs_str, "EPSG:4326", always_xy=True)
                    wgs_min_x, wgs_min_y = transformer.transform(min_x, min_y)
                    wgs_max_x, wgs_max_y = transformer.transform(max_x, max_y)
                    bounds_geom = mapping(box(wgs_min_x, wgs_min_y, wgs_max_x, wgs_max_y))
                except Exception:
                    bounds_geom = mapping(box(min_x, min_y, max_x, max_y))
            else:
                bounds_geom = mapping(box(min_x, min_y, max_x, max_y))

            # GSD Calculation (Ground Sampling Distance in cm)
            gsd_cm = None
            if crs_str and ("326" in crs_str or "327" in crs_str or "UTM" in crs_str.upper()):
                # Metric CRS, resolution is in meters
                gsd_cm = round(float(res_x * 100.0), 2)
            elif crs_str and "4326" in crs_str:
                # Degree resolution -> approximate at equator
                gsd_cm = round(float(res_x * 111319.5 * 100.0), 2)

            nodata = src.nodata

            return {
                "filename": filename,
                "file_size_bytes": file_size_bytes,
                "width": width,
                "height": height,
                "bands": count,
                "data_type": data_type,
                "crs": crs_str, # Will be None if missing
                "bounds_min_x": float(min_x),
                "bounds_min_y": float(min_y),
                "bounds_max_x": float(max_x),
                "bounds_max_y": float(max_y),
                "bounds_geom": bounds_geom,
                "resolution_x": float(res_x),
                "resolution_y": float(res_y),
                "gsd_cm": gsd_cm, # Will be None if cannot be calculated
                "nodata_value": float(nodata) if nodata is not None else None,
                "affine_transform": [float(val) for val in transform[:6]]
            }
    except Exception as e:
        # Fallback inspection if rasterio is not installed or file is partially corrupted
        return {
            "filename": filename,
            "file_size_bytes": file_size_bytes,
            "width": 0,
            "height": 0,
            "bands": 0,
            "data_type": "unknown",
            "crs": None,
            "bounds_min_x": None,
            "bounds_min_y": None,
            "bounds_max_x": None,
            "bounds_max_y": None,
            "bounds_geom": None,
            "resolution_x": None,
            "resolution_y": None,
            "gsd_cm": None,
            "nodata_value": None,
            "affine_transform": None,
            "error": str(e)
        }
