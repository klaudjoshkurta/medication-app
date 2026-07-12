package com.shkurta.medication.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.repository.MedicationRepository
import com.shkurta.medication.domain.usecase.UpdateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditMedicationUiState(
    val loaded: Boolean = false,
    val id: Long = 0L,
    val name: String = "",
    val cause: String = "",
    val description: String = "",
    val dosageMgText: String = "",
    val recurring: Boolean = false,
    val hoursText: String = "",
    val saving: Boolean = false
) {
    val hoursValue: Int? get() = hoursText.toIntOrNull()
    val dosageMgValue: Int? get() = dosageMgText.toIntOrNull()
    val canSave: Boolean
        get() = loaded &&
                name.isNotBlank() &&
                (!recurring || (hoursValue != null && hoursValue!! > 0)) &&
                !saving
}

@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
    private val updateMedicationUseCase: UpdateMedicationUseCase
) : ViewModel() {

    private val medicationId: Long = checkNotNull(savedStateHandle["medicationId"])

    private val _state = MutableStateFlow(EditMedicationUiState())
    val state: StateFlow<EditMedicationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val med = repository.getMedication(medicationId) ?: return@launch
            _state.value = EditMedicationUiState(
                loaded = true,
                id = med.id,
                name = med.name,
                cause = med.cause.orEmpty(),
                description = med.description.orEmpty(),
                dosageMgText = med.dosageMg?.toString().orEmpty(),
                recurring = med.intervalHours != null,
                hoursText = med.intervalHours?.toString().orEmpty()
            )
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onCauseChange(value: String) = _state.update { it.copy(cause = value) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value) }
    fun onDosageMgChange(value: String) =
        _state.update { it.copy(dosageMgText = value.filter { c -> c.isDigit() }) }
    fun onRecurringChange(value: Boolean) = _state.update { it.copy(recurring = value) }
    fun onHoursChange(value: String) =
        _state.update { it.copy(hoursText = value.filter { c -> c.isDigit() }) }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val interval = if (s.recurring) s.hoursValue else null
            updateMedicationUseCase(
                s.id,
                s.name,
                s.cause.takeIf { it.isNotBlank() },
                s.description.takeIf { it.isNotBlank() },
                s.dosageMgValue,
                interval
            )
            onSaved()
        }
    }
}