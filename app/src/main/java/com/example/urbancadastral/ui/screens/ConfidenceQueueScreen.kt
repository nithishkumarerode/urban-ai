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
import com.example.urbancadastral.data.local.entity.ParcelEntity
import com.example.urbancadastral.ui.components.ConfidenceBadge
import com.example.urbancadastral.ui.components.StatusBadge

@Composable
fun ConfidenceQueueScreen(
    parcels: List<ParcelEntity>,
    onUpdateStatus: (parcelId: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTier by remember { mutableStateOf("ALL") }

    val filtered = remember(parcels, selectedTier) {
        when (selectedTier) {
            "LOW" -> parcels.filter { it.confidenceScore < 0.60f }
            "MEDIUM" -> parcels.filter { it.confidenceScore in 0.60f..0.85f }
            "HIGH" -> parcels.filter { it.confidenceScore > 0.85f }
            "NEEDS_REVIEW" -> parcels.filter { it.verificationStatus == "NEEDS_REVIEW" }
            else -> parcels
        }
    }

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
                        text = "Confidence Queue & Human Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Human-in-the-loop validation queue. High confidence parcels auto-verify, while boundary shifts require cadastral approval or field surveyor dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All (${parcels.size})",
                    "LOW" to "Low <60%",
                    "MEDIUM" to "Med 60-85%",
                    "HIGH" to "High >85%",
                    "NEEDS_REVIEW" to "Review Queue"
                ).forEach { (tier, label) ->
                    FilterChip(
                        selected = selectedTier == tier,
                        onClick = { selectedTier = tier },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        items(filtered) { p ->
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
                        Column {
                            Text(text = "Parcel ${p.parcelNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(text = p.legalDeedOwner, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                        }
                        ConfidenceBadge(confidence = p.confidenceScore, tier = p.confidenceTier)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Area: ${p.areaSqm} m²", fontSize = 11.sp, color = TextPrimaryDark)
                        Text(text = "Land Use: ${p.landUse}", fontSize = 11.sp, color = GeoCyan)
                        StatusBadge(status = p.verificationStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(p.id, "NEEDS_REVIEW") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoAmber),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dispatch Survey", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onUpdateStatus(p.id, "HUMAN_APPROVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald, contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve Boundary", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
