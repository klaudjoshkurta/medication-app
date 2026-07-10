package com.shkurta.medication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shkurta.medication.domain.model.DoseLog
import com.shkurta.medication.domain.model.UpcomingDose
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    var confirmDeleteMed by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var confirmDeleteLog by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add medication")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.upcoming.isEmpty() && state.history.isEmpty()) {
            EmptyState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (state.upcoming.isNotEmpty()) {
                item { SectionHeader("Upcoming") }
                items(state.upcoming, key = { "up-${it.medicationId}" }) { dose ->
                    UpcomingRow(
                        dose = dose,
                        nowMillis = nowMillis,
                        onTakenNow = { viewModel.markTaken(dose.medicationId) },
                        onEdit = { viewModel.startEdit(dose.medicationId) },
                        onDelete = { confirmDeleteMed = dose.medicationId to dose.medicationName }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
            if (state.history.isNotEmpty()) {
                item { SectionHeader("History") }
                items(state.history, key = { "log-${it.id}" }) { log ->
                    HistoryRow(
                        log = log,
                        onEdit = { viewModel.startEdit(log.medicationId) },
                        onDelete = { confirmDeleteLog = log.id }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }

    editState?.let { es ->
        EditMedicationDialog(
            state = es,
            onNameChange = viewModel::onEditNameChange,
            onRecurringChange = viewModel::onEditRecurringChange,
            onHoursChange = viewModel::onEditHoursChange,
            onSave = viewModel::saveEdit,
            onDismiss = viewModel::cancelEdit
        )
    }

    confirmDeleteMed?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { confirmDeleteMed = null },
            title = { Text("Delete $name?") },
            text = { Text("This removes the medication and all its history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedication(id)
                    confirmDeleteMed = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteMed = null }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    confirmDeleteLog?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDeleteLog = null },
            title = { Text("Delete entry?") },
            text = { Text("This removes this dose from your history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDoseLog(id)
                    confirmDeleteLog = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteLog = null }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun UpcomingRow(
    dose: UpcomingDose,
    nowMillis: Long,
    onTakenNow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val remainingMs = (dose.scheduledAt - nowMillis).coerceAtLeast(0L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dose.medicationName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "in ${formatDuration(remainingMs)} · ${formatClock(dose.scheduledAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = onTakenNow,
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text("Taken", fontWeight = FontWeight.Medium)
        }
        RowOverflowMenu(onEdit = onEdit, onDelete = onDelete)
    }
}

@Composable
private fun HistoryRow(
    log: DoseLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.medicationName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatDateTime(log.takenAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RowOverflowMenu(onEdit = onEdit, onDelete = onDelete)
    }
}

@Composable
private fun RowOverflowMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            DropdownMenuItem(
                text = { Text("Edit", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun EditMedicationDialog(
    state: EditMedicationState,
    onNameChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onHoursChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Recurring",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(checked = state.recurring, onCheckedChange = onRecurringChange)
                }
                if (state.recurring) {
                    OutlinedTextField(
                        value = state.hoursText,
                        onValueChange = onHoursChange,
                        label = { Text("Every N hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = state.canSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No medications yet.\nTap + to add one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

private fun formatClock(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
