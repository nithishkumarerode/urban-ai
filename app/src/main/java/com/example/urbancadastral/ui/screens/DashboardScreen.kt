package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.*
import com.example.urbancadastral.ui.components.CadastralMetricCard
import com.example.urbancadastral.ui.components.StatusBadge
import com.example.urbancadastral.ui.viewmodel.CadastralScreen

@Composable
fun DashboardScreen(
    parcels: List<ParcelEntity>,
    conflicts: List<BoundaryConflictEntity>,
    datasets: List<DroneDatasetEntity>,
    jobs: List<ProcessingJobEntity>,
    tasks: List<GroundTruthTaskEntity>,
    onNavigate: (CadastralScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalParcels = parcels.size
    val reviewCount = parcels.count { it.verificationStatus == "NEEDS_REVIEW" }
    val conflictCount = conflicts.count { it.status != "RESOLVED" }
    val highConfidenceCount = parcels.count { it.confidenceScore >= 0.85f }
    val groundTruthVerifiedCount = parcels.count { it.verificationStatus == "GROUND_TRUTH_SEALED" }
    val autoVerifiedCount = parcels.count { it.verificationStatus == "AUTO_VERIFIED" }
    val humanApprovedCount = parcels.count { it.verificationStatus == "HUMAN_APPROVED" }

    val activeJob = jobs.firstOrNull { it.status == "RUNNING" } ?: jobs.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Status Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_banner")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sector 4 Cadastral Pipeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Government-grade Drone Orthomosaic & Parcel AI",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                        if (activeJob != null) {
                            StatusBadge(status = activeJob.status)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (activeJob != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Active Pipeline: ${activeJob.stage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoCyan,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${activeJob.progressPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { activeJob.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GeoCyan,
                            trackColor = CadastralBorder
                        )

                        if (activeJob.isDevSimulation) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "NOTICE: Development Simulation Mode Active (Results identified as dev testing)",
                                fontSize = 10.sp,
                                color = GeoAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Primary 6 Metrics Grid
        item {
            Text(
                text = "Cadastral Intelligence Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CadastralMetricCard(
                        title = "Total Parcels",
                        value = totalParcels.toString(),
                        subtitle = "Sector 4 Modernization",
                        icon = Icons.Default.GridOn,
                        accentColor = GeoCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.CADASTRAL_MAP) }
                    )
                    CadastralMetricCard(
                        title = "AI Extracted",
                        value = (totalParcels + 12).toString(),
                        subtitle = "Polygons & Footprints",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.FEATURE_EXTRACTION) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CadastralMetricCard(
                        title = "Parcels for Review",
                        value = reviewCount.toString(),
                        subtitle = "Confidence < 60% or Shift",
                        icon = Icons.Default.RateReview,
                        accentColor = GeoAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.CONFIDENCE_QUEUE) }
                    )
                    CadastralMetricCard(
                        title = "Boundary Conflicts",
                        value = conflictCount.toString(),
                        subtitle = "Deed/Encroachment Alerts",
                        icon = Icons.Default.WarningAmber,
                        accentColor = GeoCoral,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.BOUNDARY_CONFLICTS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CadastralMetricCard(
                        title = "High Confidence",
                        value = highConfidenceCount.toString(),
                        subtitle = "> 85% IoU Match",
                        icon = Icons.Default.Verified,
                        accentColor = GeoEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.CONFIDENCE_QUEUE) }
                    )
                    CadastralMetricCard(
                        title = "CORS Verified",
                        value = groundTruthVerifiedCount.toString(),
                        subtitle = "RTK Sealed (<2cm fix)",
                        icon = Icons.Default.GpsFixed,
                        accentColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(CadastralScreen.GROUND_TRUTH) }
                    )
                }
            }
        }

        // Cadastral Verification Progress Breakdown
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cadastral Verification Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val verifiedTotal = autoVerifiedCount + humanApprovedCount + groundTruthVerifiedCount
                    val pct = if (totalParcels > 0) ((verifiedTotal.toFloat() / totalParcels) * 100).toInt() else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Verified Rate: $verifiedTotal / $totalParcels parcels",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = GeoEmerald
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi-segment progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CadastralBorder)
                    ) {
                        if (totalParcels > 0) {
                            val autoWeight = autoVerifiedCount.toFloat() / totalParcels
                            val humanWeight = humanApprovedCount.toFloat() / totalParcels
                            val sealedWeight = groundTruthVerifiedCount.toFloat() / totalParcels
                            val reviewWeight = reviewCount.toFloat() / totalParcels

                            if (autoWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(autoWeight.coerceAtLeast(0.01f)).background(GeoEmerald))
                            if (humanWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(humanWeight.coerceAtLeast(0.01f)).background(GeoCyan))
                            if (sealedWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(sealedWeight.coerceAtLeast(0.01f)).background(Color(0xFFA855F7)))
                            if (reviewWeight > 0) Box(modifier = Modifier.fillMaxHeight().weight(reviewWeight.coerceAtLeast(0.01f)).background(GeoAmber))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LegendItem(color = GeoEmerald, label = "Auto ($autoVerifiedCount)")
                        LegendItem(color = GeoCyan, label = "Approved ($humanApprovedCount)")
                        LegendItem(color = Color(0xFFA855F7), label = "Sealed ($groundTruthVerifiedCount)")
                        LegendItem(color = GeoAmber, label = "Review ($reviewCount)")
                    }
                }
            }
        }

        // Land Use Distribution
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automated Land-Use Classification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val landUseMap = parcels.groupBy { it.landUse }
                    val colors = listOf(GeoCyan, GeoEmerald, Color(0xFFF97316), Color(0xFFA855F7), GeoBlue)
                    var cIdx = 0

                    landUseMap.forEach { (use, list) ->
                        val color = colors[cIdx % colors.size]
                        cIdx++
                        val usePct = if (totalParcels > 0) ((list.size.toFloat() / totalParcels) * 100).toInt() else 0

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = use,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimaryDark
                                    )
                                }
                                Text(
                                    text = "${list.size} parcels ($usePct%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { usePct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = color,
                                trackColor = CadastralBorder
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Launchpad
        item {
            Text(
                text = "Operational Workflow Quick-Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(CadastralScreen.DRONE_IMAGERY) },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan.copy(alpha = 0.2f), contentColor = GeoCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ingest Drone", fontSize = 12.sp)
                }

                Button(
                    onClick = { onNavigate(CadastralScreen.CADASTRAL_MAP) },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald.copy(alpha = 0.2f), contentColor = GeoEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "GIS Map", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondaryDark
        )
    }
}
