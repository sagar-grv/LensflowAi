package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.NavTab
import com.example.domain.model.ScreenState
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PcSettingsScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.ResultDetailsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.LensFlowTheme
import com.example.ui.viewmodel.LensFlowUiEvent
import com.example.ui.viewmodel.LensFlowViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: LensFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val view = LocalView.current

            LaunchedEffect(Unit) {
                viewModel.eventFlow.collectLatest { event ->
                    when (event) {
                        is LensFlowUiEvent.ShowToast -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        }
                        is LensFlowUiEvent.TriggerHapticFeedback -> {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                        is LensFlowUiEvent.OpenPdfShare -> {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, event.uri)
                                putExtra(Intent.EXTRA_SUBJECT, event.title)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Document Report"))
                        }
                        is LensFlowUiEvent.OpenCalendarIntent -> {
                            val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, event.title)
                                putExtra(CalendarContract.Events.DESCRIPTION, event.details)
                            }
                            try {
                                context.startActivity(calendarIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is LensFlowUiEvent.OpenEmailIntent -> {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_SUBJECT, "LensFlow Action Item: ${event.subject}")
                                putExtra(Intent.EXTRA_TEXT, event.body)
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            LensFlowTheme(isRedLight = uiState.isRedLightMode) {
                LensFlowApp(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun LensFlowApp(
    viewModel: LensFlowViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.screenState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            ScreenState.CAMERA_VIEW -> {
                CameraScreen(
                    selectedMode = uiState.selectedScanMode,
                    onModeSelect = { viewModel.setScanMode(it) },
                    onBack = { viewModel.setScreenState(ScreenState.MAIN_TABS) },
                    onCapture = { uri -> viewModel.triggerScan(uri, uiState.selectedScanMode) },
                    onSampleClick = { viewModel.triggerSampleScan(uiState.selectedScanMode) },
                    isOffline = uiState.isOfflineMode,
                    onToggleOffline = { viewModel.toggleOfflineMode(it) }
                )
            }
            ScreenState.PROCESSING_VIEW -> {
                ProcessingScreen(
                    mode = uiState.selectedScanMode,
                    isOffline = uiState.isOfflineMode
                )
            }
            ScreenState.RESULT_DETAILS -> {
                val current = uiState.currentScan
                if (current != null) {
                    ResultDetailsScreen(
                        record = current,
                        officeKit = uiState.officeKit,
                        onBack = { viewModel.setScreenState(ScreenState.MAIN_TABS) },
                        onToggleItem = { itemId -> viewModel.toggleActionItem(current.id, itemId) },
                        onShareToPc = { viewModel.copyToClipboard(current) },
                        onExportPdf = { viewModel.exportPdf(current) },
                        onCalendarClick = { item -> viewModel.createCalendarEvent(item) },
                        onEmailClick = { item -> viewModel.sendEmailSummary(item) },
                        onDelete = { viewModel.deleteScan(current.id) }
                    )
                } else {
                    viewModel.setScreenState(ScreenState.MAIN_TABS)
                }
            }
            ScreenState.MAIN_TABS -> {
                Scaffold(
                    contentWindowInsets = WindowInsets.navigationBars,
                    floatingActionButton = {
                        if (uiState.activeTab != NavTab.PC_SETTINGS) {
                            FloatingActionButton(
                                onClick = { viewModel.setScreenState(ScreenState.CAMERA_VIEW) },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .testTag("fab_scan_document")
                                    .semantics { role = Role.Button }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Document",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            NavigationBarItem(
                                selected = uiState.activeTab == NavTab.HOME,
                                onClick = { viewModel.setNavTab(NavTab.HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.activeTab == NavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Scans"
                                    )
                                },
                                label = { Text("Scans") },
                                modifier = Modifier.testTag("nav_tab_home"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.activeTab == NavTab.TASKS,
                                onClick = { viewModel.setNavTab(NavTab.TASKS) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.activeTab == NavTab.TASKS) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment,
                                        contentDescription = "Action Tasks"
                                    )
                                },
                                label = { Text("Action Items") },
                                modifier = Modifier.testTag("nav_tab_tasks"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.activeTab == NavTab.PC_SETTINGS,
                                onClick = { viewModel.setNavTab(NavTab.PC_SETTINGS) },
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.activeTab == NavTab.PC_SETTINGS) Icons.Filled.Laptop else Icons.Outlined.Laptop,
                                        contentDescription = "PC Link"
                                    )
                                },
                                label = { Text("PC Link") },
                                modifier = Modifier.testTag("nav_tab_pc_settings"),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (uiState.activeTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    scans = uiState.scans,
                                    selectedFilter = uiState.selectedCategoryFilter,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onFilterSelect = { viewModel.setCategoryFilter(it) },
                                    onScanClick = { viewModel.setScreenState(ScreenState.CAMERA_VIEW) },
                                    onQuickSampleClick = { mode -> viewModel.triggerSampleScan(mode) },
                                    onRecordClick = { record -> viewModel.selectScanRecord(record) },
                                    onDeleteClick = { id -> viewModel.deleteScan(id) },
                                    onToggleItem = { scanId, itemId -> viewModel.toggleActionItem(scanId, itemId) },
                                    onCopyRecord = { record -> viewModel.copyToClipboard(record) },
                                    isOffline = uiState.isOfflineMode,
                                    geminiApiKey = uiState.geminiApiKey,
                                    onToggleOffline = { viewModel.toggleOfflineMode(it) },
                                    onSaveApiKey = { viewModel.saveGeminiApiKey(it) },
                                    telemetry = uiState.telemetry
                                )
                            }
                            NavTab.TASKS -> {
                                TasksScreen(
                                    scans = uiState.scans,
                                    onToggleItem = { scanId, itemId -> viewModel.toggleActionItem(scanId, itemId) },
                                    onCalendarClick = { item -> viewModel.createCalendarEvent(item) },
                                    onEmailClick = { item -> viewModel.sendEmailSummary(item) },
                                    onMarkAllCompleted = { viewModel.markAllTasksCompleted() },
                                    onCopyAllTasks = { viewModel.copyAllTasksToClipboard() },
                                    onAddActionItem = { scanId, title, type -> viewModel.addActionItem(scanId, title, type) }
                                )
                            }
                            NavTab.PC_SETTINGS -> {
                                PcSettingsScreen(
                                    officeKit = uiState.officeKit,
                                    telemetry = uiState.telemetry,
                                    isOffline = uiState.isOfflineMode,
                                    onToggleOffline = { viewModel.toggleOfflineMode(it) },
                                    isRedLightMode = uiState.isRedLightMode,
                                    onToggleRedLight = { viewModel.toggleRedLightMode(it) },
                                    onToggleMirror = { viewModel.toggleScreenMirror() },
                                    onToggleClipboardSync = { viewModel.toggleClipboardSync(it) },
                                    geminiApiKey = uiState.geminiApiKey,
                                    isTestingKey = uiState.isTestingKey,
                                    onSaveApiKey = { viewModel.saveGeminiApiKey(it) },
                                    onClearApiKey = { viewModel.clearGeminiApiKey() },
                                    onTestApiKey = { viewModel.testGeminiConnection(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
