package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.BoundaryConflictEntity
import com.example.urbancadastral.ui.components.StatusBadge

@Composable
fun BoundaryConflictsScreen(
    conflicts: List<BoundaryConflictEntity>,
    onResolveConflict: (conflictId: Long, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var resolvingConflict by remember { mutableStateOf<BoundaryConflictEntity?>(null) }

    val filteredList = remember(conflicts, selectedFilter) {
        when (selectedFilter) {
            "CRITICAL" -> conflicts.filter { it.severity == "CRITICAL" }
            "OPEN" -> conflicts.filter { it.status != "RESOLVED" }
            "RESOLVED" -> conflicts.filter { it.status == "RESOLVED" }
            else -> conflicts
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cadastral Boundary Conflicts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Discrepancy audit between deed legal registry & AI physical boundaries",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeoCoral.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${conflicts.count { it.status != "RESOLVED" }} Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoCoral
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All Discrepancies", "CRITICAL" to "Critical", "OPEN" to "Open", "RESOLVED" to "Resolved").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (key == "CRITICAL") GeoCoral.copy(alpha = 0.25f) else GeoCyan.copy(alpha = 0.2f),
                            selectedLabelColor = if (key == "CRITICAL") GeoCoral else GeoCyan
                        )
                    )
                }
            }
        }

        // List
        items(filteredList) { conflict ->
            val isCritical = conflict.severity == "CRITICAL"
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isCritical) GeoCoral.copy(alpha = 0.6f) else CadastralBorder
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("conflict_item_${conflict.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isCritical) GeoCoral else GeoAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${conflict.parcelNumber} ↔ ${conflict.adjacentParcelNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        StatusBadge(status = conflict.severity)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = conflict.conflictType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = conflict.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CadastralBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Encroachment Distance", fontSize = 10.sp, color = TextSecondaryDark)
                            Text("${conflict.encroachmentDistanceM} m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCritical) GeoCoral else TextPrimaryDark)
                        }
                        Column {
                            Text("Disputed Area", fontSize = 10.sp, color = TextSecondaryDark)
                            Text("${conflict.disputedAreaSqm} m²", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }
                        Column {
                            Text("Workflow Status", fontSize = 10.sp, color = TextSecondaryDark)
                            StatusBadge(status = conflict.status)
                        }
                    }

                    if (conflict.status != "RESOLVED") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { resolvingConflict = conflict },
                                colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald, contentColor = Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("btn_resolve_conflict_${conflict.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resolve Dispute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (resolvingConflict != null) {
        var resolutionNotes by remember { mutableStateOf("Boundary alignment reconciled via joint surveyor field confirmation.") }
        AlertDialog(
            onDismissRequest = { resolvingConflict = null },
            title = {
                Text(
                    text = "Resolve Boundary Conflict #${resolvingConflict?.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter cadastral reconciliation notes or survey boundary compromise agreement:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        resolvingConflict?.let { onResolveConflict(it.id, resolutionNotes) }
                        resolvingConflict = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald, contentColor = Color.White)
                ) {
                    Text("Confirm Resolution", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { resolvingConflict = null }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CadastralSurfaceDark
        )
    }
}
