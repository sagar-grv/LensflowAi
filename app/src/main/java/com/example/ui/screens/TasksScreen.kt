package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActionItem
import com.example.domain.model.ScanRecord
import com.example.ui.components.ActionItemRow

@Composable
fun TasksScreen(
    scans: List<ScanRecord>,
    onToggleItem: (scanId: String, itemId: String) -> Unit,
    onCalendarClick: (ActionItem) -> Unit,
    onEmailClick: (ActionItem) -> Unit,
    onMarkAllCompleted: () -> Unit = {},
    onCopyAllTasks: () -> Unit = {},
    onAddActionItem: (scanId: String, title: String, type: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var taskFilter by remember { mutableStateOf("All") }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskType by remember { mutableStateOf("TASK") }

    val allItemsWithScan = scans.flatMap { scan ->
        scan.items.map { item -> Pair(scan.id, item) }
    }

    val totalItems = allItemsWithScan.size
    val completedItems = allItemsWithScan.count { it.second.isChecked }
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems.toFloat() else 0f

    val filteredItems = when (taskFilter) {
        "Pending" -> allItemsWithScan.filter { !it.second.isChecked }
        "Completed" -> allItemsWithScan.filter { it.second.isChecked }
        else -> allItemsWithScan
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Header & Progress ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Action Items Checklist",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Extracted across physical documents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (scans.isNotEmpty()) {
                IconButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier.testTag("add_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom task",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Progress Overview Card
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Completion",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedItems of $totalItems done (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tasks_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                // Quick Action Bar
                if (totalItems > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMarkAllCompleted,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_all_done_button"),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Mark All Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onCopyAllTasks,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_all_tasks_button"),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Copy Checklist",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Filter Chips ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf("All", "Pending", "Completed")
            items(filters) { filter ->
                val isSelected = taskFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { taskFilter = filter },
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

        // --- 3. Action Items List ---
        if (filteredItems.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (totalItems == 0) "No Extracted Action Items" else "No $taskFilter Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (totalItems == 0) "Scan any receipt, whiteboard, or document to populate your checklist automatically." else "Great job! All items in this category have been checked off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredItems.forEach { (scanId, item) ->
                    ActionItemRow(
                        item = item,
                        onToggle = { onToggleItem(scanId, item.id) },
                        onCalendarClick = { onCalendarClick(item) },
                        onEmailClick = { onEmailClick(item) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog && scans.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Action Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Description") },
                        placeholder = { Text("e.g., Follow up with accountant") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            onAddActionItem(scans.first().id, newTaskTitle.trim(), newTaskType)
                            newTaskTitle = ""
                            showAddTaskDialog = false
                        }
                    },
                    enabled = newTaskTitle.isNotBlank()
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

