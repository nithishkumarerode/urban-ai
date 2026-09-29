package com.example.urbancadastral.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.BuildingFootprintEntity
import com.example.urbancadastral.data.local.entity.ParcelEntity
import com.example.urbancadastral.ui.viewmodel.GisLayerConfig
import org.json.JSONArray

@Composable
fun CadastralGisCanvas(
    parcels: List<ParcelEntity>,
    buildings: List<BuildingFootprintEntity>,
    selectedParcel: ParcelEntity?,
    onParcelSelected: (ParcelEntity?) -> Unit,
    layers: GisLayerConfig,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.7f, 4.0f)
        offset += offsetChange
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .testTag("gis_interactive_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(parcels, scale, offset) {
                    detectTapGestures { tapOffset ->
                        // Hit test parcels
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Transform tap back to normalized coordinate space
                        val localX = (tapOffset.x - offset.x) / (canvasWidth * scale)
                        val localY = (tapOffset.y - offset.y) / (canvasHeight * scale)

                        var tappedParcel: ParcelEntity? = null
                        for (parcel in parcels) {
                            try {
                                val jsonArr = JSONArray(parcel.geometryJson)
                                var minX = 1.0; var maxX = 0.0; var minY = 1.0; var maxY = 0.0
                                for (i in 0 until jsonArr.length()) {
                                    val pt = jsonArr.getJSONArray(i)
                                    val px = pt.getDouble(0)
                                    val py = pt.getDouble(1)
                                    if (px < minX) minX = px
                                    if (px > maxX) maxX = px
                                    if (py < minY) minY = py
                                    if (py > maxY) maxY = py
                                }
                                if (localX in minX..maxX && localY in minY..maxY) {
                                    tappedParcel = parcel
                                    break
                                }
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                        onParcelSelected(tappedParcel)
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Drone Orthomosaic Aerial Background Grid
            if (layers.showOrthomosaic) {
                drawDroneAerialBackground(width, height, scale, offset)
            }

            // 2. Right-of-Way Road Corridors
            if (layers.showRoads) {
                drawRoadRightOfWays(width, height, scale, offset)
            }

            // 3. Cadastral Parcel Polygons
            if (layers.showParcels) {
                for (parcel in parcels) {
                    val isSelected = parcel.id == selectedParcel?.id
                    drawParcelPolygon(
                        parcel = parcel,
                        width = width,
                        height = height,
                        scale = scale,
                        offset = offset,
                        isSelected = isSelected,
                        textMeasurer = textMeasurer,
                        showConflicts = layers.showConflicts,
                        showHeatmap = layers.showConfidenceHeatmap
                    )
                }
            }

            // 4. Extracted Building Footprints (3D Extrusion Representation)
            if (layers.showBuildings) {
                drawBuildingFootprints(buildings, parcels, width, height, scale, offset)
            }

            // 5. CORS RTK Survey Ground-Truth Station Marks
            if (layers.showCorsMarks) {
                drawCorsSurveyBenchmarks(width, height, scale, offset, textMeasurer)
            }
        }

        // HUD Overlay Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CadastralSurfaceDark.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = GeoCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "UTM 33N | 2.1cm GSD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${(scale * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeoCyan
            )
        }

        // Zoom Reset & Center Button
        FloatingActionButton(
            onClick = {
                scale = 1.0f
                offset = Offset.Zero
            },
            containerColor = CadastralCardDark,
            contentColor = GeoCyan,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Reset View",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun DrawScope.drawDroneAerialBackground(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    // Aerial orthomosaic simulation pattern with high-contrast GIS grid
    val gridStep = 40.dp.toPx() * scale
    val startX = (offset.x % gridStep)
    val startY = (offset.y % gridStep)

    // Base satellite terrain color
    drawRect(
        color = Color(0xFF131D2E),
        topLeft = Offset(0f, 0f),
        size = Size(width, height)
    )

    // Sub-urban vegetation and paved terrain patches
    val patchPaint = Paint().apply {
        color = Color(0xFF1B2B3E)
        style = PaintingStyle.Fill
    }

    var x = startX
    while (x < width) {
        drawLine(
            color = Color(0xFF1E2D4A),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += gridStep
    }

    var y = startY
    while (y < height) {
        drawLine(
            color = Color(0xFF1E2D4A),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += gridStep
    }
}

private fun DrawScope.drawRoadRightOfWays(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    // Draw public access road running between parcels
    val roadY = (height * 0.36f * scale) + offset.y
    val roadHeight = 24.dp.toPx() * scale

    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(0f, roadY),
        size = Size(width, roadHeight)
    )
    // Road center line
    drawLine(
        color = Color(0xFFE2E8F0).copy(alpha = 0.4f),
        start = Offset(0f, roadY + roadHeight / 2),
        end = Offset(width, roadY + roadHeight / 2),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    )
    // Right of Way boundary buffer
    drawLine(
        color = GeoAmber.copy(alpha = 0.5f),
        start = Offset(0f, roadY),
        end = Offset(width, roadY),
        strokeWidth = 1.5f
    )
    drawLine(
        color = GeoAmber.copy(alpha = 0.5f),
        start = Offset(0f, roadY + roadHeight),
        end = Offset(width, roadY + roadHeight),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawParcelPolygon(
    parcel: ParcelEntity,
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset,
    isSelected: Boolean,
    textMeasurer: TextMeasurer,
    showConflicts: Boolean,
    showHeatmap: Boolean
) {
    try {
        val jsonArr = JSONArray(parcel.geometryJson)
        if (jsonArr.length() < 3) return

        val path = Path()
        val points = mutableListOf<Offset>()

        for (i in 0 until jsonArr.length()) {
            val pt = jsonArr.getJSONArray(i)
            val normX = pt.getDouble(0).toFloat()
            val normY = pt.getDouble(1).toFloat()
            val px = (normX * width * scale) + offset.x
            val py = (normY * height * scale) + offset.y
            points.add(Offset(px, py))
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()

        val fillColor = when {
            isSelected -> GeoCyan.copy(alpha = 0.30f)
            showHeatmap -> {
                when {
                    parcel.confidenceScore >= 0.85f -> GeoEmerald.copy(alpha = 0.25f)
                    parcel.confidenceScore >= 0.60f -> GeoAmber.copy(alpha = 0.25f)
                    else -> GeoCoral.copy(alpha = 0.30f)
                }
            }
            showConflicts && parcel.hasConflict -> GeoCoral.copy(alpha = 0.22f)
            else -> Color(0xFF00B4D8).copy(alpha = 0.12f)
        }

        val strokeColor = when {
            isSelected -> GeoCyan
            showConflicts && parcel.hasConflict -> GeoCoral
            parcel.verificationStatus == "GROUND_TRUTH_SEALED" -> Color(0xFFA78BFA)
            parcel.verificationStatus == "AUTO_VERIFIED" -> GeoEmerald
            else -> Color(0xFF38BDF8)
        }

        drawPath(path, color = fillColor)
        drawPath(path, color = strokeColor, style = Stroke(width = if (isSelected) 3.5f else 2.0f))

        // Draw boundary vertex control pins
        for (pt in points) {
            drawCircle(
                color = strokeColor,
                radius = if (isSelected) 5f else 3.5f,
                center = pt
            )
        }

        // Draw Parcel Label
        val center = Offset(
            points.map { it.x }.average().toFloat(),
            points.map { it.y }.average().toFloat()
        )

        drawText(
            textMeasurer = textMeasurer,
            text = parcel.parcelNumber,
            topLeft = Offset(center.x - 28f, center.y - 10f),
            style = TextStyle(
                color = if (isSelected) GeoCyan else TextPrimaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Conflict indicator badge if conflict flagged
        if (showConflicts && parcel.hasConflict) {
            drawCircle(
                color = GeoCoral,
                radius = 8f,
                center = Offset(center.x + 28f, center.y)
            )
        }

    } catch (e: Exception) {
        // geometry parse fallback
    }
}

private fun DrawScope.drawBuildingFootprints(
    buildings: List<BuildingFootprintEntity>,
    parcels: List<ParcelEntity>,
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    // Draw building footprints inside parcels
    for (b in buildings) {
        val parcel = parcels.find { it.id == b.parcelId } ?: continue
        try {
            val jsonArr = JSONArray(parcel.geometryJson)
            if (jsonArr.length() < 3) continue

            val p0 = jsonArr.getJSONArray(0)
            val p2 = jsonArr.getJSONArray(2)

            val minX = p0.getDouble(0).toFloat()
            val minY = p0.getDouble(1).toFloat()
            val maxX = p2.getDouble(0).toFloat()
            val maxY = p2.getDouble(1).toFloat()

            // Sub-box for building footprint inside parcel
            val bMinX = minX + (maxX - minX) * 0.20f
            val bMaxX = minX + (maxX - minX) * 0.75f
            val bMinY = minY + (maxY - minY) * 0.22f
            val bMaxY = minY + (maxY - minY) * 0.72f

            val px = (bMinX * width * scale) + offset.x
            val py = (bMinY * height * scale) + offset.y
            val bw = ((bMaxX - bMinX) * width * scale)
            val bh = ((bMaxY - bMinY) * height * scale)

            // Extruded shadow representation (from DSM/DTM height)
            val heightExtrude = (b.estimatedHeightMeters * 0.8f).toFloat() * scale
            drawRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(px + heightExtrude, py - heightExtrude),
                size = Size(bw, bh)
            )

            // Rooftop polygon
            val rooftopColor = if (b.isEncroaching) Color(0xFFDC2626) else Color(0xFFD97706)
            drawRect(
                color = rooftopColor.copy(alpha = 0.85f),
                topLeft = Offset(px, py),
                size = Size(bw, bh)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(px, py),
                size = Size(bw, bh),
                style = Stroke(width = 1.5f)
            )
        } catch (e: Exception) {
            // ignore
        }
    }
}

private fun DrawScope.drawCorsSurveyBenchmarks(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset,
    textMeasurer: TextMeasurer
) {
    // Benchmark 1
    val bx1 = (0.21f * width * scale) + offset.x
    val by1 = (0.15f * height * scale) + offset.y

    drawCircle(color = Color(0xFFA855F7), radius = 9f, center = Offset(bx1, by1))
    drawCircle(color = Color.White, radius = 4f, center = Offset(bx1, by1))

    drawText(
        textMeasurer = textMeasurer,
        text = "CORS-REF-04 [±1.2cm]",
        topLeft = Offset(bx1 + 12f, by1 - 8f),
        style = TextStyle(color = Color(0xFFC084FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )

    // Benchmark 2
    val bx2 = (0.75f * width * scale) + offset.x
    val by2 = (0.65f * height * scale) + offset.y

    drawCircle(color = Color(0xFFA855F7), radius = 9f, center = Offset(bx2, by2))
    drawCircle(color = Color.White, radius = 4f, center = Offset(bx2, by2))

    drawText(
        textMeasurer = textMeasurer,
        text = "CORS-BER-01 [±0.8cm]",
        topLeft = Offset(bx2 + 12f, by2 - 8f),
        style = TextStyle(color = Color(0xFFC084FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    )
}
