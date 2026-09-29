package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class CadastralChangeItem(
    val parcelNumber: String,
    val changeType: String,
    val description: String,
    val baselineDate: String,
    val currentFlightDate: String,
    val severity: String,
    val areaShiftSqm: Double
)

@Composable
fun ChangeDetectionScreen(modifier: Modifier = Modifier) {
    val changes = listOf(
        CadastralChangeItem(
            parcelNumber = "PAR-8402",
            changeType = "New Unauthorized Structure",
            description = "Unregistered annex warehouse footprint (+140 m²) constructed along north lot line without building permit.",
            baselineDate = "Oct 2024 Drone Ortho",
            currentFlightDate = "Sept 2026 Flight #4",
            severity = "CRITICAL",
            areaShiftSqm = 140.0
        ),
        CadastralChangeItem(
            parcelNumber = "PAR-8404",
            changeType = "Fence Line Physical Shift",
            description = "Masonry perimeter wall advanced 0.95m forward into the municipal pedestrian right-of-way buffer.",
            baselineDate = "Jan 2025 Cadastral Baseline",
            currentFlightDate = "Sept 2026 Flight #4",
            severity = "MODERATE",
            areaShiftSqm = 18.2
        ),
        CadastralChangeItem(
            parcelNumber = "PAR-8405",
            changeType = "Approved Property Subdivision",
            description = "Lot split into dual residential parcels with new demarcated timber boundary.",
            baselineDate = "May 2025 Registry",
            currentFlightDate = "Sept 2026 Flight #4",
            severity = "INFO",
            areaShiftSqm = 340.0
        )
    )

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
                        text = "Temporal Drone Change Detection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automated computer vision comparison between historic geodetic baselines and latest drone orthomosaics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        items(changes.size) { idx ->
            val change = changes[idx]
            val isCritical = change.severity == "CRITICAL"
            val badgeColor = when (change.severity) {
                "CRITICAL" -> GeoCoral
                "MODERATE" -> GeoAmber
                else -> GeoEmerald
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Compare,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Parcel ${change.parcelNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = change.changeType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = change.description, style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Baseline: ${change.baselineDate}", fontSize = 10.sp, color = TextSecondaryDark)
                        Text(text = "Latest: ${change.currentFlightDate}", fontSize = 10.sp, color = GeoCyan)
                        Text(text = "Shift: ${change.areaShiftSqm} m²", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }
                }
            }
        }
    }
}
