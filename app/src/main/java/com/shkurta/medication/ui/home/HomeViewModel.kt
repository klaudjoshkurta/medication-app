package com.shkurta.medication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.repository.MedicationRepository
import com.shkurta.medication.domain.usecase.DeleteDoseLogUseCase
import com.shkurta.medication.domain.usecase.DeleteMedicationUseCase
import com.shkurta.medication.domain.usecase.MarkDoseTakenUseCase
import com.shkurta.medication.domain.usecase.ObserveTimelineUseCase
import com.shkurta.medication.domain.usecase.UpdateMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditMedicationState(
    val id: Long,
    val name: String,
    val cause: String,
    val description: String,
    val dosageMgText: String,
    val recurring: Boolean,
    val hoursText: String
) {
    val hoursValue: Int? get() = hoursText.toIntOrNull()
    val dosageMgValue: Int? get() = dosageMgText.toIntOrNull()
    val canSave: Boolean
        get() = name.isNotBlank() && (!recurring || (hoursValue != null && hoursValue!! > 0))
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTimelineUseCase: ObserveTimelineUseCase,
    private val repository: MedicationRepository,
    private val markDoseTakenUseCase: MarkDoseTakenUseCase,
    private val updateMedicationUseCase: UpdateMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val deleteDoseLogUseCase: DeleteDoseLogUseCase
) : ViewModel() {

    val state: StateFlow<TimelineState> = observeTimelineUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimelineState(upcoming = emptyList(), history = emptyList())
        )

    private val _editState = MutableStateFlow<EditMedicationState?>(null)
    val editState: StateFlow<EditMedicationState?> = _editState.asStateFlow()

    fun markTaken(medicationId: Long) {
        viewModelScope.launch { markDoseTakenUseCase(medicationId) }
    }

    fun startEdit(medicationId: Long) {
        viewModelScope.launch {
            val med = repository.getMedication(medicationId) ?: return@launch
            _editState.value = EditMedicationState(
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

    fun cancelEdit() {
        _editState.value = null
    }

    fun onEditNameChange(value: String) =
        _editState.update { it?.copy(name = value) }

    fun onEditCauseChange(value: String) =
        _editState.update { it?.copy(cause = value) }

    fun onEditDescriptionChange(value: String) =
        _editState.update { it?.copy(description = value) }

    fun onEditDosageMgChange(value: String) =
        _editState.update { it?.copy(dosageMgText = value.filter { c -> c.isDigit() }) }

    fun onEditRecurringChange(value: Boolean) =
        _editState.update { it?.copy(recurring = value) }

    fun onEditHoursChange(value: String) =
        _editState.update { it?.copy(hoursText = value.filter { c -> c.isDigit() }) }

    fun saveEdit() {
        val s = _editState.value ?: return
        if (!s.canSave) return
        val interval = if (s.recurring) s.hoursValue else null
        viewModelScope.launch {
            updateMedicationUseCase(
                s.id,
                s.name,
                s.cause.takeIf { it.isNotBlank() },
                s.description.takeIf { it.isNotBlank() },
                s.dosageMgValue,
                interval
            )
            _editState.value = null
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch { deleteMedicationUseCase(id) }
    }

    fun deleteDoseLog(id: Long) {
        viewModelScope.launch { deleteDoseLogUseCase(id) }
    }
}
