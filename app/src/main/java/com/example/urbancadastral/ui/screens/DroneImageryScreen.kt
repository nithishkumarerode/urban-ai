package com.example.urbancadastral.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.urbancadastral.data.local.entity.DroneDatasetEntity
import com.example.urbancadastral.data.local.entity.ProcessingJobEntity
import com.example.urbancadastral.ui.components.StatusBadge

@Composable
fun DroneImageryScreen(
    datasets: List<DroneDatasetEntity>,
    jobs: List<ProcessingJobEntity>,
    onUploadDataset: (
        name: String,
        fileType: String,
        fileSizeMb: Double,
        crs: String,
        gsdCm: Double,
        resolution: String,
        coverageHa: Double,
        extentBBox: String,
        sensorModel: String
    ) -> Unit,
    onRunJob: (jobId: Long, isDevMode: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showUploadDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CadastralCardDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CadastralBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Drone Imagery & ORI/DSM Ingestion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload GeoTIFF/COG, DSM/DTM, LAS/LAZ datasets with automated CRS and GSD validation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { showUploadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_upload_dataset")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Ingest Dataset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Real Drone Orthomosaic Preview
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
                        Text(
                            text = "Active Orthomosaic Imagery Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GeoEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Cloud-Optimized GeoTIFF", fontSize = 10.sp, color = GeoEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Aerial photo asset
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.drone_orthomosaic),
                            contentDescription = "Drone Orthomosaic Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Technical Overlay HUD
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sector 7 RGB | 16800x14200 px | 2.1 cm GSD | EPSG:32633",
                                fontSize = 10.sp,
                                color = GeoCyan,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Ingested Datasets List
        item {
            Text(
                text = "Ingested Datasets (${datasets.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark
            )
        }

        items(datasets) { dataset ->
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
                                imageVector = if (dataset.fileType.contains("DSM")) Icons.Default.Terrain else Icons.Default.Image,
                                contentDescription = null,
                                tint = GeoCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = dataset.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "${dataset.fileType} • ${dataset.fileSizeMb} MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                        StatusBadge(status = dataset.validationStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CadastralBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetadataItem(label = "CRS", value = dataset.crs)
                        MetadataItem(label = "GSD", value = "${dataset.gsdCm} cm/px")
                        MetadataItem(label = "Coverage", value = "${dataset.coverageHa} ha")
                        MetadataItem(label = "Resolution", value = dataset.resolution)
                    }
                }
            }
        }

        // Processing Jobs Section
        item {
            Text(
                text = "AI Processing Pipelines & Jobs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark
            )
        }

        items(jobs) { job ->
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
                            Text(
                                text = "Job #${job.id}: ${job.datasetName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Stage: ${job.stage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoCyan
                            )
                        }
                        StatusBadge(status = job.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { job.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (job.isDevSimulation) GeoAmber else GeoEmerald,
                        trackColor = CadastralBorder
                    )

                    if (job.isDevSimulation) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠ DEVELOPMENT SIMULATION MODE: Results generated for UI/testing, not certified extraction.",
                            fontSize = 10.sp,
                            color = GeoAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (job.status != "RUNNING") {
                            OutlinedButton(
                                onClick = { onRunJob(job.id, true) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoAmber),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeoAmber.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("btn_run_dev_pipeline")
                            ) {
                                Text(text = "Dev Simulation", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onRunJob(job.id, false) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoEmerald, contentColor = Color.White),
                                modifier = Modifier.testTag("btn_run_prod_pipeline")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Run AI Pipeline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Upload & Ingest Dialog
    if (showUploadDialog) {
        UploadDatasetDialog(
            onDismiss = { showUploadDialog = false },
            onConfirm = { name, fileType, sizeMb, crs, gsd, res, coverage, extent, sensor ->
                onUploadDataset(name, fileType, sizeMb, crs, gsd, res, coverage, extent, sensor)
                showUploadDialog = false
            }
        )
    }
}

@Composable
private fun MetadataItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextSecondaryDark)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
    }
}

@Composable
private fun UploadDatasetDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        fileType: String,
        sizeMb: Double,
        crs: String,
        gsd: Double,
        resolution: String,
        coverage: Double,
        extent: String,
        sensor: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("Flight_05_Sector8_RGB.tif") }
    var fileType by remember { mutableStateOf("GeoTIFF (COG)") }
    var crs by remember { mutableStateOf("EPSG:32633") }
    var gsdStr by remember { mutableStateOf("2.3") }
    var sensor by remember { mutableStateOf("DJI Zenmuse P1 (45MP)") }
    var coverageStr by remember { mutableStateOf("115.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ingest Drone Survey Dataset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dataset File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = crs,
                    onValueChange = { crs = it },
                    label = { Text("Coordinate Reference System (CRS)") },
                    supportingText = { Text("e.g. EPSG:32633 (UTM 33N)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gsdStr,
                        onValueChange = { gsdStr = it },
                        label = { Text("GSD (cm/px)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = coverageStr,
                        onValueChange = { coverageStr = it },
                        label = { Text("Coverage (ha)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = sensor,
                    onValueChange = { sensor = it },
                    label = { Text("Sensor / Drone Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gsd = gsdStr.toDoubleOrNull() ?: 2.5
                    val cov = coverageStr.toDoubleOrNull() ?: 100.0
                    onConfirm(
                        name,
                        fileType,
                        284.5,
                        crs,
                        gsd,
                        "14400 x 11200 px",
                        cov,
                        "13.385, 52.512 to 13.415, 52.535",
                        sensor
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D))
            ) {
                Text("Validate & Ingest", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = CadastralSurfaceDark
    )
}
