package com.shkurta.medication.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.model.DoseLog
import com.shkurta.medication.domain.model.Medication
import com.shkurta.medication.domain.model.UpcomingDose
import com.shkurta.medication.domain.repository.MedicationRepository
import com.shkurta.medication.domain.usecase.DeleteDoseLogUseCase
import com.shkurta.medication.domain.usecase.MarkDoseTakenUseCase
import com.shkurta.medication.domain.usecase.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationDetailsState(
    val medication: Medication? = null,
    val history: List<DoseLog> = emptyList(),
    val nextDose: UpcomingDose? = null
)

@HiltViewModel
class MedicationDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTimelineUseCase: ObserveTimelineUseCase,
    private val repository: MedicationRepository,
    private val markDoseTakenUseCase: MarkDoseTakenUseCase,
    private val deleteDoseLogUseCase: DeleteDoseLogUseCase
) : ViewModel() {

    private val medicationId: Long = checkNotNull(savedStateHandle["medicationId"])

    private val _medication = MutableStateFlow<Medication?>(null)

    val state: StateFlow<MedicationDetailsState> =
        combine(_medication, observeTimelineUseCase()) { med, timeline ->
            MedicationDetailsState(
                medication = med,
                history = timeline.history.filter { it.medicationId == medicationId },
                nextDose = timeline.upcoming.firstOrNull { it.medicationId == medicationId }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationDetailsState()
        )

    init {
        viewModelScope.launch {
            _medication.value = repository.getMedication(medicationId)
        }
    }

    fun markTaken() {
        viewModelScope.launch { markDoseTakenUseCase(medicationId) }
    }

    fun deleteDoseLog(id: Long) {
        viewModelScope.launch { deleteDoseLogUseCase(id) }
    }
}
