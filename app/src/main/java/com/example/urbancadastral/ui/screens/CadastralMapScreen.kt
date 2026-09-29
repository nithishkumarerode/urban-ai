package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.BuildingFootprintEntity
import com.example.urbancadastral.data.local.entity.ParcelEntity
import com.example.urbancadastral.ui.components.CadastralGisCanvas
import com.example.urbancadastral.ui.components.ConfidenceBadge
import com.example.urbancadastral.ui.components.StatusBadge
import com.example.urbancadastral.ui.viewmodel.GisLayerConfig

@Composable
fun CadastralMapScreen(
    parcels: List<ParcelEntity>,
    buildings: List<BuildingFootprintEntity>,
    selectedParcel: ParcelEntity?,
    onSelectParcel: (ParcelEntity?) -> Unit,
    onUpdateStatus: (parcelId: Long, status: String) -> Unit,
    layers: GisLayerConfig,
    onToggleLayer: (
        showOrtho: Boolean?,
        showParcels: Boolean?,
        showBuildings: Boolean?,
        showRoads: Boolean?,
        showConflicts: Boolean?,
        showHeatmap: Boolean?,
        showCors: Boolean?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Layer Toggles Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = layers.showOrthomosaic,
                onClick = { onToggleLayer(!layers.showOrthomosaic, null, null, null, null, null, null) },
                label = { Text("Drone Ortho", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Satellite, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeoCyan.copy(alpha = 0.2f),
                    selectedLabelColor = GeoCyan
                )
            )

            FilterChip(
                selected = layers.showParcels,
                onClick = { onToggleLayer(null, !layers.showParcels, null, null, null, null, null) },
                label = { Text("Parcels", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.CropFree, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeoCyan.copy(alpha = 0.2f),
                    selectedLabelColor = GeoCyan
                )
            )

            FilterChip(
                selected = layers.showBuildings,
                onClick = { onToggleLayer(null, null, !layers.showBuildings, null, null, null, null) },
                label = { Text("Buildings (3D)", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.HomeWork, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFD97706).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFFBBF24)
                )
            )

            FilterChip(
                selected = layers.showRoads,
                onClick = { onToggleLayer(null, null, null, !layers.showRoads, null, null, null) },
                label = { Text("Roads & RoW", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.AltRoute, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeoAmber.copy(alpha = 0.2f),
                    selectedLabelColor = GeoAmber
                )
            )

            FilterChip(
                selected = layers.showConflicts,
                onClick = { onToggleLayer(null, null, null, null, !layers.showConflicts, null, null) },
                label = { Text("Conflicts", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.WarningAmber, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeoCoral.copy(alpha = 0.2f),
                    selectedLabelColor = GeoCoral
                )
            )

            FilterChip(
                selected = layers.showConfidenceHeatmap,
                onClick = { onToggleLayer(null, null, null, null, null, !layers.showConfidenceHeatmap, null) },
                label = { Text("Confidence Heatmap", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeoEmerald.copy(alpha = 0.2f),
                    selectedLabelColor = GeoEmerald
                )
            )

            FilterChip(
                selected = layers.showCorsMarks,
                onClick = { onToggleLayer(null, null, null, null, null, null, !layers.showCorsMarks) },
                label = { Text("CORS RTK", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.GpsFixed, null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA855F7).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFA855F7)
                )
            )

            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CadastralCardDark)
                    .size(32.dp)
                    .testTag("btn_export_gis")
            ) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export GIS", tint = GeoCyan, modifier = Modifier.size(16.dp))
            }
        }

        // Interactive GIS Canvas
        CadastralGisCanvas(
            parcels = parcels,
            buildings = buildings,
            selectedParcel = selectedParcel,
            onParcelSelected = onSelectParcel,
            layers = layers,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (selectedParcel != null) 1.1f else 2.0f)
        )

        // Parcel Inspector Bottom Panel
        if (selectedParcel != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ParcelInspectorCard(
                parcel = selectedParcel,
                buildings = buildings.filter { it.parcelId == selectedParcel.id },
                onClose = { onSelectParcel(null) },
                onUpdateStatus = onUpdateStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CadastralCardDark.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = GeoCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tap any cadastral polygon to inspect 3D property details, boundary vertices, and verification status.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }
    }

    if (showExportDialog) {
        GisExportDialog(
            parcels = parcels,
            buildings = buildings,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun ParcelInspectorCard(
    parcel: ParcelEntity,
    buildings: List<BuildingFootprintEntity>,
    onClose: () -> Unit,
    onUpdateStatus: (parcelId: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
        modifier = modifier.testTag("parcel_inspector_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Parcel ${parcel.parcelNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ConfidenceBadge(confidence = parcel.confidenceScore, tier = parcel.confidenceTier)
                    }
                    Text(
                        text = parcel.legalDeedOwner,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CadastralBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Attributes Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Area", fontSize = 10.sp, color = TextSecondaryDark)
                    Text("${parcel.areaSqm} m²", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
                Column {
                    Text("Perimeter", fontSize = 10.sp, color = TextSecondaryDark)
                    Text("${parcel.perimeterMeters} m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
                Column {
                    Text("Land Use", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(parcel.landUse, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GeoCyan)
                }
                Column {
                    Text("Zoning", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(parcel.zoningCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Extruded Structures
            Text(
                text = "3D Building Footprints (DSM Derived)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24)
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (buildings.isEmpty()) {
                Text(
                    text = "No detected structures (Vacant / Agricultural parcel)",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            } else {
                for (b in buildings) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CadastralSurfaceDark)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = b.structureType, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            Text(text = "Footprint: ${b.footprintAreaSqm} m²", fontSize = 10.sp, color = TextSecondaryDark)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Height: ${b.estimatedHeightMeters}m", fontSize = 10.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onUpdateStatus(parcel.id, "NEEDS_REVIEW") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Flag Review", fontSize = 11.sp)
                }

                Button(
                    onClick = { onUpdateStatus(parcel.id, "HUMAN_APPROVED") },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GisExportDialog(
    parcels: List<ParcelEntity>,
    buildings: List<BuildingFootprintEntity>,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val geoJsonSummary = remember(parcels) {
        val features = parcels.joinToString(",\n") { p ->
            """    {
      "type": "Feature",
      "properties": {
        "parcel_id": "${p.parcelNumber}",
        "owner": "${p.legalDeedOwner}",
        "area_sqm": ${p.areaSqm},
        "land_use": "${p.landUse}",
        "confidence": ${p.confidenceScore},
        "status": "${p.verificationStatus}"
      },
      "geometry": {
        "type": "Polygon",
        "coordinates": [${p.geometryJson}]
      }
    }"""
        }
        """{
  "type": "FeatureCollection",
  "crs": { "type": "name", "properties": { "name": "urn:ogc:def:crs:EPSG::32633" } },
  "features": [
$features
  ]
}"""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export GIS Cadastral Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Formats: GeoJSON (RFC 7946), GeoPackage (GPKG), ESRI Shapefile, Cadastral Registry CSV.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, CadastralBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = geoJsonSummary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = GeoCyan
                    )
                }

                if (copied) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ GeoJSON copied to clipboard!",
                        fontSize = 11.sp,
                        color = GeoEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(geoJsonSummary))
                    copied = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D))
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy GeoJSON", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondaryDark)
            }
        },
        containerColor = CadastralSurfaceDark
    )
}
