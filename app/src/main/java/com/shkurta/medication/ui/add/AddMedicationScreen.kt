package com.shkurta.medication.ui.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onDone: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add medication",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        bottomBar = {
            SaveBar(
                canSave = state.canSave,
                saving = state.saving,
                validationHint = validationHint(state),
                onCancel = onDone,
                onSave = { viewModel.save(onSaved = onDone) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FormSection(label = "Details") {
                MonoTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Name",
                    placeholder = "e.g. Ibuprofen"
                )
                MonoTextField(
                    value = state.cause,
                    onValueChange = viewModel::onCauseChange,
                    label = "Cause",
                    placeholder = "e.g. Headache"
                )
                MonoTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Description",
                    placeholder = "e.g. Pill"
                )
            }

            FormSection(label = "Dosage") {
                MonoTextField(
                    value = state.dosageMgText,
                    onValueChange = viewModel::onDosageMgChange,
                    label = "Dosage",
                    suffix = "mg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            FormSection(label = "Schedule") {
                RecurringToggleCard(
                    recurring = state.recurring,
                    hoursValue = state.hoursValue,
                    onToggle = viewModel::onRecurringChange
                )
                if (state.recurring) {
                    IntervalHoursField(
                        hoursText = state.hoursText,
                        hoursValue = state.hoursValue,
                        onChange = viewModel::onHoursChange,
                        isError = state.hoursText.isNotBlank() && state.hoursValue == null
                    )
                }
            }

            FormSection(label = "First dose") {
                TakenAtControls(
                    millis = state.takenAtMillis,
                    onChange = viewModel::onTakenAtChange
                )
            }
        }
    }
}

@Composable
private fun SaveBar(
    canSave: Boolean,
    saving: Boolean,
    validationHint: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (validationHint != null && !saving) {
                Text(
                    text = validationHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !saving,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = onSave,
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Saving…", fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Save medication", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    suffix: String? = null,
    singleLine: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = placeholder?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.onBackground,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.onBackground,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun MonoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.background,
            checkedTrackColor = MaterialTheme.colorScheme.onBackground,
            checkedBorderColor = MaterialTheme.colorScheme.onBackground,
            checkedIconColor = MaterialTheme.colorScheme.onBackground,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun IntervalHoursField(
    hoursText: String,
    hoursValue: Int?,
    onChange: (String) -> Unit,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8, 12, 24).forEach { p ->
                IntervalChip(
                    hours = p,
                    selected = hoursValue == p,
                    onClick = { onChange(p.toString()) }
                )
            }
        }
        MonoTextField(
            value = hoursText,
            onValueChange = onChange,
            label = "Interval",
            suffix = "hours",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError
        )
    }
}

@Composable
private fun IntervalChip(
    hours: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
    val subFg = if (selected) {
        MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = border,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "${hours}h",
                style = MaterialTheme.typography.titleMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = frequencyLabel(hours),
                style = MaterialTheme.typography.labelSmall,
                color = subFg
            )
        }
    }
}

private fun frequencyLabel(hours: Int): String = when {
    hours <= 0 -> ""
    24 % hours == 0 -> {
        val perDay = 24 / hours
        if (perDay == 1) "daily" else "$perDay/day"
    }
    else -> "every ${hours}h"
}

@Composable
private fun RecurringToggleCard(
    recurring: Boolean,
    hoursValue: Int?,
    onToggle: (Boolean) -> Unit
) {
    val border = if (recurring) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
    } else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!recurring) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recurring dose",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = recurringSubtitle(recurring, hoursValue),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MonoSwitch(
                checked = recurring,
                onCheckedChange = onToggle
            )
        }
    }
}

private fun recurringSubtitle(recurring: Boolean, hoursValue: Int?): String = when {
    !recurring -> "One-time dose"
    hoursValue == null || hoursValue <= 0 -> "Choose interval below"
    hoursValue == 1 -> "Every hour"
    else -> "Every $hoursValue hours"
}

private fun validationHint(state: AddMedicationUiState): String? = when {
    state.saving -> null
    state.name.isBlank() -> "Add a name to save"
    state.recurring && (state.hoursValue == null || state.hoursValue!! <= 0) -> "Enter how often to repeat"
    else -> null
}

@Composable
private fun FormSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.weight(1f)
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TakenAtControls(
    millis: Long,
    onChange: (Long) -> Unit
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FieldCard(
            label = "Date",
            value = formatDate(millis),
            onClick = { showDate = true },
            modifier = Modifier.weight(1f)
        )
        FieldCard(
            label = "Time",
            value = formatTime(millis),
            onClick = { showTime = true },
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PresetChip("Now") { onChange(System.currentTimeMillis()) }
        PresetChip("5m ago") { onChange(System.currentTimeMillis() - 5 * 60_000L) }
        PresetChip("1h ago") { onChange(System.currentTimeMillis() - 60 * 60_000L) }
    }

    if (showDate) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = millis)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val newDateUtc = dateState.selectedDateMillis
                    if (newDateUtc != null) {
                        onChange(combineDateAndTime(dateUtcMillis = newDateUtc, timeMillis = millis))
                    }
                    showDate = false
                }) { Text("Set date") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("Cancel") }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Set time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(replaceTime(millis, timeState.hour, timeState.minute))
                    showTime = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun FieldCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PresetChip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun combineDateAndTime(dateUtcMillis: Long, timeMillis: Long): Long {
    val localDate = Instant.ofEpochMilli(dateUtcMillis)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val localDateTime = localDate.atTime(
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE)
    )
    return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun replaceTime(millis: Long, hour: Int, minute: Int): Long {
    val cal = java.util.Calendar.getInstance().apply {
        timeInMillis = millis
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
