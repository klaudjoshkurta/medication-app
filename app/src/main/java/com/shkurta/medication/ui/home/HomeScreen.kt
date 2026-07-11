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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shkurta.medication.domain.model.DoseLog
import com.shkurta.medication.domain.model.Medication
import com.shkurta.medication.domain.model.UpcomingDose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    var showMedsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var confirmDeleteMed by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var confirmDeleteLog by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                actions = {
                    IconButton(onClick = { showMedsSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Medications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
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
        if (state.upcoming.isEmpty() && state.history.isEmpty() && state.medications.isEmpty()) {
            EmptyState(
                onAddClick = onAddClick,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        val nextDose = state.upcoming.firstOrNull()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (nextDose != null) {
                item {
                    UpcomingRow(
                        dose = nextDose,
                        nowMillis = nowMillis,
                        onTakenNow = { viewModel.markTaken(nextDose.medicationId) }
                    )
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
                }
            }
        }
    }

    if (showMedsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMedsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            MedicationsSheetContent(
                medications = state.medications,
                onTakeNow = { medId ->
                    viewModel.markTaken(medId)
                    dismissSheet(scope, sheetState) { showMedsSheet = false }
                },
                onEdit = { medId ->
                    viewModel.startEdit(medId)
                    dismissSheet(scope, sheetState) { showMedsSheet = false }
                },
                onDelete = { med ->
                    confirmDeleteMed = med.id to med.name
                    dismissSheet(scope, sheetState) { showMedsSheet = false }
                }
            )
        }
    }

    editState?.let { es ->
        EditMedicationDialog(
            state = es,
            onNameChange = viewModel::onEditNameChange,
            onCauseChange = viewModel::onEditCauseChange,
            onDescriptionChange = viewModel::onEditDescriptionChange,
            onDosageMgChange = viewModel::onEditDosageMgChange,
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

@OptIn(ExperimentalMaterial3Api::class)
private fun dismissSheet(
    scope: CoroutineScope,
    sheetState: SheetState,
    onHidden: () -> Unit
) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) onHidden()
    }
}

@Composable
private fun MedicationsSheetContent(
    medications: List<Medication>,
    onTakeNow: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Medication) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Your medications",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (medications.isEmpty()) {
            Text(
                text = "No medications added yet.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(medications, key = { it.id }) { med ->
                    ListItem(
                        headlineContent = {
                            Text(
                                med.name,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        supportingContent = {
                            val info = buildString {
                                med.dosageMg?.let { append("${it}mg") }
                                med.intervalHours?.let {
                                    if (isNotEmpty()) append(" · ")
                                    append("Every ${it}h")
                                }
                                med.cause?.let {
                                    if (isNotEmpty()) append(" · ")
                                    append(it)
                                }
                            }
                            if (info.isNotEmpty()) {
                                Text(
                                    info,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            MedicationRowMenu(
                                onTakeNow = { onTakeNow(med.id) },
                                onEdit = { onEdit(med.id) },
                                onDelete = { onDelete(med) }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationRowMenu(
    onTakeNow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                text = { Text("Take now", color = MaterialTheme.colorScheme.onBackground) },
                onClick = {
                    expanded = false
                    onTakeNow()
                }
            )
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
) {
    val diffMs = dose.scheduledAt - nowMillis
    val isOverdue = diffMs < 0
    val absMs = if (isOverdue) -diffMs else diffMs

    val windowMs = 60L * 60L * 1000L
    val progress = if (isOverdue) {
        1f
    } else {
        (1f - (diffMs.toFloat() / windowMs.toFloat())).coerceIn(0f, 1f)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dose.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    val subtitle = buildString {
                        dose.medicationCause?.let { append(it) }
                        dose.medicationDescription?.let {
                            if (isNotEmpty()) append(" · ")
                            append(it)
                        }
                    }
                    if (subtitle.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatClock(dose.scheduledAt),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isOverdue) {
                    "Overdue by ${formatCompactDuration(absMs)}"
                } else {
                    "In ${formatCompactDuration(absMs)}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTakenNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Mark as taken", fontWeight = FontWeight.Medium)
            }
        }
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
            val subtitle = buildString {
                log.medicationCause?.let { append(it) }
                log.medicationDescription?.let {
                    if (isNotEmpty()) append(" · ")
                    append(it)
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    onCauseChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDosageMgChange: (String) -> Unit,
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
                OutlinedTextField(
                    value = state.cause,
                    onValueChange = onCauseChange,
                    label = { Text("Cause") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.dosageMgText,
                    onValueChange = onDosageMgChange,
                    label = { Text("Dosage (mg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "No medications yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Add your first medication to start tracking doses.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add medication")
            }
        }
    }
}

private fun formatCompactDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60

    return when {
        h >= 1 && m > 0 -> "${h}h ${m}m"
        h >= 1 -> "${h}h"
        m >= 1 -> "${m}m"
        else -> "less than a minute"
    }
}

private fun formatClock(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))

private fun formatTodayHeader(millis: Long): String =
    "Today · ${SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(millis))}"
