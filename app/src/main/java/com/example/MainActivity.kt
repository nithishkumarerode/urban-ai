package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CadastralNavyDark
import com.example.ui.theme.GeoCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.urbancadastral.ui.components.CadastralNavigationDrawerContent
import com.example.urbancadastral.ui.components.CadastralTopBar
import com.example.urbancadastral.ui.screens.*
import com.example.urbancadastral.ui.viewmodel.CadastralScreen
import com.example.urbancadastral.ui.viewmodel.CadastralViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CadastralViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                UrbanCadastralApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UrbanCadastralApp(viewModel: CadastralViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val parcels by viewModel.parcels.collectAsStateWithLifecycle()
    val datasets by viewModel.datasets.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val footprints by viewModel.buildingFootprints.collectAsStateWithLifecycle()
    val conflicts by viewModel.boundaryConflicts.collectAsStateWithLifecycle()
    val tasks by viewModel.groundTruthTasks.collectAsStateWithLifecycle()
    val evidenceList by viewModel.allEvidence.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    val selectedParcel by viewModel.selectedParcel.collectAsStateWithLifecycle()
    val selectedTaskId by viewModel.selectedTaskId.collectAsStateWithLifecycle()
    val layers by viewModel.gisLayers.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CadastralNavigationDrawerContent(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    viewModel.navigateTo(screen)
                    coroutineScope.launch { drawerState.close() }
                },
                conflictCount = conflicts.count { it.status != "RESOLVED" },
                pendingTaskCount = tasks.count { it.status != "VERIFIED_SEALED" }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CadastralTopBar(
                    title = currentScreen.title,
                    subtitle = currentScreen.category,
                    onMenuClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    actions = {
                        if (currentScreen != CadastralScreen.CADASTRAL_MAP) {
                            IconButton(onClick = { viewModel.navigateTo(CadastralScreen.CADASTRAL_MAP) }) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Quick Map",
                                    tint = GeoCyan
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = CadastralNavyDark,
            contentWindowInsets = WindowInsets.navigationBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CadastralNavyDark)
            ) {
                when (currentScreen) {
                    CadastralScreen.DASHBOARD -> DashboardScreen(
                        parcels = parcels,
                        conflicts = conflicts,
                        datasets = datasets,
                        jobs = jobs,
                        tasks = tasks,
                        onNavigate = { viewModel.navigateTo(it) }
                    )

                    CadastralScreen.DRONE_IMAGERY -> DroneImageryScreen(
                        datasets = datasets,
                        jobs = jobs,
                        onUploadDataset = { name, type, size, crs, gsd, res, cov, extent, sensor ->
                            viewModel.uploadDroneDataset(name, type, size, crs, gsd, res, cov, extent, sensor)
                        },
                        onRunJob = { jobId, isDev ->
                            viewModel.runAiPipeline(jobId, isDev)
                        }
                    )

                    CadastralScreen.CADASTRAL_MAP -> CadastralMapScreen(
                        parcels = parcels,
                        buildings = footprints,
                        selectedParcel = selectedParcel,
                        onSelectParcel = { viewModel.selectParcel(it) },
                        onUpdateStatus = { id, status -> viewModel.updateParcelStatus(id, status) },
                        layers = layers,
                        onToggleLayer = { o, p, b, r, c, h, crs ->
                            viewModel.toggleLayer(o, p, b, r, c, h, crs)
                        }
                    )

                    CadastralScreen.FEATURE_EXTRACTION -> FeatureExtractionScreen(
                        parcels = parcels,
                        buildings = footprints
                    )

                    CadastralScreen.BOUNDARY_CONFLICTS -> BoundaryConflictsScreen(
                        conflicts = conflicts,
                        onResolveConflict = { id, notes -> viewModel.resolveConflict(id, notes) }
                    )

                    CadastralScreen.CONFIDENCE_QUEUE -> ConfidenceQueueScreen(
                        parcels = parcels,
                        onUpdateStatus = { id, status -> viewModel.updateParcelStatus(id, status) }
                    )

                    CadastralScreen.GROUND_TRUTH -> GroundTruthCorsScreen(
                        tasks = tasks,
                        evidenceList = evidenceList,
                        selectedTaskId = selectedTaskId,
                        onSelectTask = { viewModel.selectGroundTruthTask(it) },
                        onUploadEvidence = { taskId, fileName, type, size, surveyor, notes ->
                            viewModel.uploadEvidence(taskId, fileName, type, size, surveyor, notes)
                        },
                        onDeleteEvidence = { ev -> viewModel.deleteEvidence(ev) },
                        onSealTask = { id, surveyor -> viewModel.sealSurveyTask(id, surveyor) }
                    )

                    CadastralScreen.CHANGE_DETECTION -> ChangeDetectionScreen()

                    CadastralScreen.PROJECTS -> ProjectsScreen(
                        projects = projects,
                        onCreateProject = { name, code, jur, crs, count ->
                            viewModel.createProject(name, code, jur, crs, count)
                        }
                    )
                }
            }
        }
    }
}
