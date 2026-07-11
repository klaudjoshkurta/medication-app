package com.shkurta.medication.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMedicationUiState(
    val name: String = "",
    val cause: String = "",
    val description: String = "",
    val dosageMgText: String = "",
    val recurring: Boolean = false,
    val hoursText: String = "",
    val takenAtMillis: Long = System.currentTimeMillis(),
    val saving: Boolean = false
) {
    val hoursValue: Int? get() = hoursText.toIntOrNull()
    val dosageMgValue: Int? get() = dosageMgText.toIntOrNull()
    val canSave: Boolean
        get() = name.isNotBlank() && (!recurring || (hoursValue != null && hoursValue!! > 0)) && !saving
}

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddMedicationUiState())
    val state: StateFlow<AddMedicationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onCauseChange(value: String) = _state.update { it.copy(cause = value) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value) }
    fun onDosageMgChange(value: String) =
        _state.update { it.copy(dosageMgText = value.filter { c -> c.isDigit() }) }
    fun onRecurringChange(value: Boolean) = _state.update { it.copy(recurring = value) }
    fun onHoursChange(value: String) =
        _state.update { it.copy(hoursText = value.filter { c -> c.isDigit() }) }

    fun onTakenAtChange(millis: Long) = _state.update { it.copy(takenAtMillis = millis) }
    fun resetTakenAtToNow() = _state.update { it.copy(takenAtMillis = System.currentTimeMillis()) }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val interval = if (s.recurring) s.hoursValue else null
            addMedicationUseCase(
                s.name,
                s.cause.takeIf { it.isNotBlank() },
                s.description.takeIf { it.isNotBlank() },
                s.dosageMgValue,
                interval,
                s.takenAtMillis
            )
            _state.update { AddMedicationUiState() }
            onSaved()
        }
    }
}
