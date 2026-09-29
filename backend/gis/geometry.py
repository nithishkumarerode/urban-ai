import math
from typing import Tuple, List, Optional, Dict, Any
from shapely.geometry import shape, mapping, Polygon, MultiPolygon, Point
from shapely.ops import transform

def get_projected_crs_for_bounds(min_lon: float, min_lat: float, max_lon: float, max_lat: float) -> str:
    """
    Determines an appropriate projected UTM zone CRS based on longitude/latitude bounds.
    Never calculates area in raw lat/long degrees.
    """
    center_lon = (min_lon + max_lon) / 2.0
    center_lat = (min_lat + max_lat) / 2.0
    
    # Calculate UTM Zone
    utm_zone = int((center_lon + 180) / 6) + 1
    is_northern = center_lat >= 0
    epsg_code = 32600 + utm_zone if is_northern else 32700 + utm_zone
    return f"EPSG:{epsg_code}"

def calculate_real_area_and_perimeter(
    geojson_geom: Dict[str, Any],
    source_crs: str = "EPSG:4326",
    target_projected_crs: Optional[str] = None
) -> Tuple[float, float, float]:
    """
    Calculates precise metric area (sqm), hectares, and perimeter (meters)
    using a projected CRS (UTM or user-specified projected system).
    """
    geom = shape(geojson_geom)
    if geom.is_empty:
        return 0.0, 0.0, 0.0

    bounds = geom.bounds # minx, miny, maxx, maxy
    if not target_projected_crs:
        target_projected_crs = get_projected_crs_for_bounds(bounds[0], bounds[1], bounds[2], bounds[3])

    try:
        import pyproj
        project_to_metric = pyproj.Transformer.from_crs(
            source_crs, target_projected_crs, always_xy=True
        ).transform
        projected_geom = transform(project_to_metric, geom)
    except Exception:
        # High precision pure-Python local metric projection fallback (WGS84 spheroid)
        center_lat = (bounds[1] + bounds[3]) / 2.0
        lat_rad = math.radians(center_lat)
        m_per_deg_lat = 111132.954 - 559.822 * math.cos(2 * lat_rad) + 1.175 * math.cos(4 * lat_rad)
        m_per_deg_lon = 111412.84 * math.cos(lat_rad) - 93.5 * math.cos(3 * lat_rad)

        def to_metric_coords(x, y, z=None):
            return (x * m_per_deg_lon, y * m_per_deg_lat)

        projected_geom = transform(to_metric_coords, geom)
    
    area_sqm = float(projected_geom.area)
    perimeter_m = float(projected_geom.length)
    area_hectares = float(area_sqm / 10000.0)

    return round(area_sqm, 2), round(area_hectares, 4), round(perimeter_m, 2)

def check_building_parcel_containment(
    building_geom_geojson: Dict[str, Any],
    parcel_geom_geojson: Dict[str, Any]
) -> bool:
    """
    Computes spatial containment or significant intersection between building footprint and parcel.
    """
    b_shape = shape(building_geom_geojson)
    p_shape = shape(parcel_geom_geojson)

    if b_shape.is_empty or p_shape.is_empty:
        return False

    # Check if building centroid or > 50% area intersects parcel
    if p_shape.contains(b_shape.centroid):
        return True

    intersection = p_shape.intersection(b_shape)
    if not intersection.is_empty and (intersection.area / b_shape.area) > 0.5:
        return True

    return False
