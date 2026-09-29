package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.GroundTruthTaskEntity
import com.example.urbancadastral.data.local.entity.SurveyEvidenceEntity
import com.example.urbancadastral.ui.components.StatusBadge

@Composable
fun GroundTruthCorsScreen(
    tasks: List<GroundTruthTaskEntity>,
    evidenceList: List<SurveyEvidenceEntity>,
    selectedTaskId: Long?,
    onSelectTask: (Long?) -> Unit,
    onUploadEvidence: (taskId: Long, fileName: String, fileType: String, sizeKb: Long, surveyor: String, notes: String) -> Unit,
    onDeleteEvidence: (SurveyEvidenceEntity) -> Unit,
    onSealTask: (taskId: Long, surveyorName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEvidenceDialogForTask by remember { mutableStateOf<GroundTruthTaskEntity?>(null) }
    var sealingTask by remember { mutableStateOf<GroundTruthTaskEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Module Banner
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
                                text = "Ground Truth & CORS RTK Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Field survey validation, millimeter CORS benchmark fixes & evidence sealing",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFA855F7).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CORS Online",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC084FC)
                            )
                        }
                    }
                }
            }
        }

        // Active Field Tasks
        item {
            Text(
                text = "Cadastral Field Tasks (${tasks.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark
            )
        }

        items(tasks) { task ->
            val isExpanded = task.id == selectedTaskId
            val taskEvidence = evidenceList.filter { it.taskId == task.id }
            val isSealed = task.status == "VERIFIED_SEALED"

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isSealed) Color(0xFFA855F7).copy(alpha = 0.5f) else CadastralBorder
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("survey_task_${task.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSealed) Icons.Default.VerifiedUser else Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isSealed) Color(0xFFA855F7) else GeoCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task #${task.id}: ${task.parcelNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        StatusBadge(status = task.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Discrepancy: ${task.discrepancyReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoAmber,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Surveyor: ${task.assignedSurveyor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "Fix: ±${task.gnssAccuracyCm}cm (${task.corsStationId})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = GeoEmerald
                        )
                    }

                    if (isSealed && task.sealedDigitalSignature != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = "Official Cadastral Digital Seal:", fontSize = 10.sp, color = TextSecondaryDark)
                                Text(
                                    text = task.sealedDigitalSignature,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFC084FC),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CadastralBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Evidence Header & Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTask(if (isExpanded) null else task.id) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attached Survey Evidence (${taskEvidence.size} files)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoCyan
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = GeoCyan
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (taskEvidence.isEmpty()) {
                            Text(
                                text = "No evidence files attached yet. Upload RTK GNSS logs, field photos, or deeds.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        } else {
                            taskEvidence.forEach { ev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CadastralSurfaceDark)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = ev.fileName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                        Text(text = "${ev.fileType} • ${ev.fileSizeKb} KB • ${ev.uploadedBy}", fontSize = 10.sp, color = TextSecondaryDark)
                                        if (ev.notes.isNotEmpty()) {
                                            Text(text = ev.notes, fontSize = 10.sp, color = GeoCyan)
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteEvidence(ev) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Evidence", tint = GeoCoral, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showAddEvidenceDialogForTask = task },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeoCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Evidence", fontSize = 11.sp)
                            }

                            if (!isSealed) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { sealingTask = task },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Seal & Certify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload Evidence Dialog
    if (showAddEvidenceDialogForTask != null) {
        val task = showAddEvidenceDialogForTask!!
        var fileName by remember { mutableStateOf("RTK_GNSS_FieldFix_Peg_${task.parcelNumber}.obs") }
        var fileType by remember { mutableStateOf("GNSS_RTK") }
        var notes by remember { mutableStateOf("Multi-frequency CORS network fixed solution.") }

        AlertDialog(
            onDismissRequest = { showAddEvidenceDialogForTask = null },
            title = {
                Text(
                    text = "Upload Survey Evidence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("GNSS_RTK", "FIELD_PHOTO", "DEED_PDF").forEach { type ->
                            FilterChip(
                                selected = fileType == type,
                                onClick = { fileType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Surveyor Notes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUploadEvidence(task.id, fileName, fileType, 1250L, task.assignedSurveyor, notes)
                        showAddEvidenceDialogForTask = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D))
                ) {
                    Text("Upload", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEvidenceDialogForTask = null }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CadastralSurfaceDark
        )
    }

    // Seal Confirmation Dialog
    if (sealingTask != null) {
        val task = sealingTask!!
        AlertDialog(
            onDismissRequest = { sealingTask = null },
            title = {
                Text(
                    text = "Seal & Certify Survey Task #${task.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are applying an official government cadastral digital signature to Parcel ${task.parcelNumber}. This will lock the verified boundary polygon and mark discrepancy as resolved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Surveyor: ${task.assignedSurveyor}\nCORS Station: ${task.corsStationId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoCyan
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSealTask(task.id, task.assignedSurveyor)
                        sealingTask = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White)
                ) {
                    Text("Apply Digital Seal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sealingTask = null }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CadastralSurfaceDark
        )
    }
}
