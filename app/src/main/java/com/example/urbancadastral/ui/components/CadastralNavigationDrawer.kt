package com.example.urbancadastral.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.urbancadastral.ui.viewmodel.CadastralScreen

@Composable
fun CadastralNavigationDrawerContent(
    currentScreen: CadastralScreen,
    onSelectScreen: (CadastralScreen) -> Unit,
    conflictCount: Int,
    pendingTaskCount: Int,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerContainerColor = CadastralSurfaceDark,
        drawerContentColor = TextPrimaryDark,
        modifier = modifier.widthIn(max = 320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GeoCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "UrbanCadastral AI",
                        tint = GeoCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "UrbanCadastral AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Land Intelligence Platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoCyan
                    )
                }
            }

            HorizontalDivider(color = CadastralBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(CadastralScreen.values()) { screen ->
                    val isSelected = screen == currentScreen
                    val (icon, badgeCount, badgeColor) = when (screen) {
                        CadastralScreen.DASHBOARD -> Triple(Icons.Default.Dashboard, 0, Color.Transparent)
                        CadastralScreen.PROJECTS -> Triple(Icons.Default.FolderSpecial, 0, Color.Transparent)
                        CadastralScreen.DRONE_IMAGERY -> Triple(Icons.Default.FlightTakeoff, 0, Color.Transparent)
                        CadastralScreen.CADASTRAL_MAP -> Triple(Icons.Default.Map, 0, Color.Transparent)
                        CadastralScreen.FEATURE_EXTRACTION -> Triple(Icons.Default.AutoFixHigh, 0, Color.Transparent)
                        CadastralScreen.BOUNDARY_CONFLICTS -> Triple(Icons.Default.WarningAmber, conflictCount, GeoCoral)
                        CadastralScreen.CONFIDENCE_QUEUE -> Triple(Icons.Default.FactCheck, 0, Color.Transparent)
                        CadastralScreen.GROUND_TRUTH -> Triple(Icons.Default.GpsFixed, pendingTaskCount, GeoAmber)
                        CadastralScreen.CHANGE_DETECTION -> Triple(Icons.Default.CompareArrows, 0, Color.Transparent)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GeoCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onSelectScreen(screen) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("nav_item_${screen.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) GeoCyan else TextSecondaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimaryDark else TextSecondaryDark
                            )
                        }

                        if (badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CadastralBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer / System Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GeoEmerald)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "System Online: REST & Room Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark
                )
            }
        }
    }
}
