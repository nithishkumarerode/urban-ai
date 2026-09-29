package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.BuildingFootprintEntity
import com.example.urbancadastral.data.local.entity.ParcelEntity
import com.example.urbancadastral.ui.components.ConfidenceBadge

@Composable
fun FeatureExtractionScreen(
    parcels: List<ParcelEntity>,
    buildings: List<BuildingFootprintEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Building Footprints (3D)", "Roads & RoW", "Land-Use Classification")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Feature Extraction Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deep learning segmentation (U-Net & SAM) extracting structures, road buffers, and land use from drone imagery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CadastralCardDark,
                contentColor = GeoCyan
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Building Footprints
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detected Structures (${buildings.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "DSM Height Extrusion Active",
                            fontSize = 10.sp,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(buildings) { b ->
                    val parcel = parcels.find { it.id == b.parcelId }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.HomeWork, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = b.structureType,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                }
                                ConfidenceBadge(confidence = b.confidenceScore, tier = "HIGH")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Parcel: ${parcel?.parcelNumber ?: "PAR-N/A"}", fontSize = 11.sp, color = GeoCyan)
                                Text("Area: ${b.footprintAreaSqm} m²", fontSize = 11.sp, color = TextPrimaryDark)
                                Text("DSM Height: ${b.estimatedHeightMeters}m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                            }

                            if (b.isEncroaching) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠ Boundary Alert: Building overhangs parcel lot line by >0.5m",
                                    fontSize = 11.sp,
                                    color = GeoCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Roads & RoW
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Right-of-Way (RoW) Buffer Analysis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GeoAmber
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Public Access Corridor #4: Buffer 12.0m (Standard). 1 detected encroachment on PAR-8404 (0.95m).\n• Arterial Northway: Buffer 24.0m. Clear of structural intrusions.\n• Pedestrian Easement: Buffer 3.5m. Clear.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryDark,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            2 -> {
                // Land-Use Classification
                items(parcels) { p ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = p.parcelNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                Text(text = "Zoning: ${p.zoningCode}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeoCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = p.landUse, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
