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
import com.example.urbancadastral.data.local.entity.CadastralProjectEntity
import com.example.urbancadastral.ui.components.StatusBadge

@Composable
fun ProjectsScreen(
    projects: List<CadastralProjectEntity>,
    onCreateProject: (name: String, code: String, jurisdiction: String, crs: String, targetParcels: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cadastral Projects Administration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Multi-jurisdiction cadastral modernization and drone re-survey campaigns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(projects) { prj ->
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
                            Text(text = prj.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(text = "Code: ${prj.code}", style = MaterialTheme.typography.bodySmall, color = GeoCyan)
                        }
                        StatusBadge(status = prj.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Jurisdiction: ${prj.jurisdiction}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                    Text(text = "Geodetic CRS: ${prj.crs}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Target Scope: ${prj.targetParcels} cadastral parcels", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("Riverside Industrial Cadastral Modernization") }
        var code by remember { mutableStateOf("PRJ-2026-RIVER3") }
        var jurisdiction by remember { mutableStateOf("Regional Land & Survey Dept") }
        var crs by remember { mutableStateOf("EPSG:32633 (UTM 33N)") }
        var targets by remember { mutableStateOf("320") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Initiate Cadastral Project", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Project Code") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jurisdiction, onValueChange = { jurisdiction = it }, label = { Text("Jurisdiction Agency") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = crs, onValueChange = { crs = it }, label = { Text("Geodetic CRS") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = targets, onValueChange = { targets = it }, label = { Text("Target Parcels") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateProject(name, code, jurisdiction, crs, targets.toIntOrNull() ?: 100)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoCyan, contentColor = Color(0xFF00363D))
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = CadastralSurfaceDark
        )
    }
}
