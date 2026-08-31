package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ScanRecord
import com.example.domain.model.TelemetryState
import com.example.ui.components.DocumentCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TelemetryCard

@Composable
fun HomeScreen(
    scans: List<ScanRecord>,
    selectedFilter: String = "All",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onFilterSelect: (String) -> Unit = {},
    onScanClick: () -> Unit = {},
    onQuickSampleClick: (String) -> Unit = {},
    onRecordClick: (ScanRecord) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onToggleItem: (scanId: String, itemId: String) -> Unit = { _, _ -> },
    onCopyRecord: (ScanRecord) -> Unit = {},
    isOffline: Boolean = true,
    geminiApiKey: String = "",
    onToggleOffline: (Boolean) -> Unit = {},
    onSaveApiKey: (String) -> Unit = {},
    telemetry: TelemetryState = TelemetryState(),
    modifier: Modifier = Modifier
) {
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempApiKey by remember { mutableStateOf(geminiApiKey) }

    val quickPresets = listOf(
        Triple("Receipt", "Expense & Tax Totals", Icons.AutoMirrored.Filled.ReceiptLong),
        Triple("Whiteboard", "Milestones & Roadmap", Icons.Default.DashboardCustomize),
        Triple("Business Card", "Contacts & Follow-Up", Icons.Default.Badge),
        Triple("Invoice", "Vendor Billing & Items", Icons.Default.RequestQuote),
        Triple("Notes", "Agendas & Action Items", Icons.Default.EditNote)
    )

    val filterOptions = listOf("All", "Receipt", "Whiteboard", "Business Card", "Invoice", "Notes")

    val filteredScans = scans.filter { scan ->
        if (selectedFilter == "All") true else scan.type.equals(selectedFilter, ignoreCase = true)
    }

    val totalTasks = scans.flatMap { it.items }.size
    val completedTasks = scans.flatMap { it.items }.count { it.isChecked }
    val pendingTasks = totalTasks - completedTasks

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Top Header & Interactive Mode Switcher ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "LensFlow AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Camera & OCR productivity hub",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Interactive Mode Toggle Chip
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (!isOffline) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        if (isOffline) {
                            if (geminiApiKey.isBlank()) {
                                showApiKeyDialog = true
                            } else {
                                onToggleOffline(false)
                            }
                        } else {
                            onToggleOffline(true)
                        }
                    }
                    .testTag("mode_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (!isOffline) Icons.Default.AutoAwesome else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (!isOffline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isOffline) "On-Device" else "Gemini AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!isOffline) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- 2. Interactive Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_search_input"),
            placeholder = { Text("Search scanned text, titles, items...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        // --- 3. Live Task & Metric Overview Banner ---
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Action Items Checklist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (pendingTasks == 0 && totalTasks > 0) "All scanned tasks completed! Great work."
                               else if (totalTasks == 0) "Scan any document or whiteboard to generate tasks."
                               else "$pendingTasks pending • $completedTasks completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "$pendingTasks",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // --- 4. Quick Presets Carousel ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Quick Document Presets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() }
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPresets) { preset ->
                    PresetCard(
                        title = preset.first,
                        description = preset.second,
                        icon = preset.third,
                        onClick = { onQuickSampleClick(preset.first) }
                    )
                }
            }
        }

        // --- 5. Productivity Overview Card ---
        TelemetryCard(
            telemetry = telemetry,
            isOffline = isOffline,
            hasApiKey = geminiApiKey.isNotBlank(),
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            onToggleEngine = {
                if (isOffline && geminiApiKey.isBlank()) {
                    showApiKeyDialog = true
                } else {
                    onToggleOffline(!isOffline)
                }
            }
        )

        // --- 6. Filter Chips Row ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scanned Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "${filteredScans.size} documents",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelect(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // --- 7. Documents List / Empty State ---
            if (filteredScans.isEmpty()) {
                EmptyStateView(
                    onScanClick = onScanClick,
                    onQuickSampleClick = onQuickSampleClick
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredScans.forEach { scan ->
                        DocumentCard(
                            record = scan,
                            onClick = { onRecordClick(scan) },
                            onDelete = { onDeleteClick(scan.id) },
                            onToggleItem = { itemId -> onToggleItem(scan.id, itemId) },
                            onCopy = { onCopyRecord(scan) }
                        )
                    }
                }
            }
        }
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Google Gemini API Key") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To enable cloud AI analysis with Gemini 2.5 Flash, enter your Google AI Studio API key.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveApiKey(tempApiKey)
                        onToggleOffline(false)
                        showApiKeyDialog = false
                    }
                ) {
                    Text("Save & Enable Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PresetCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
            }
            .testTag("preset_card_$title")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

