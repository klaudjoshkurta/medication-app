package com.shkurta.medication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.usecase.DeleteDoseLogUseCase
import com.shkurta.medication.domain.usecase.DeleteMedicationUseCase
import com.shkurta.medication.domain.usecase.MarkDoseTakenUseCase
import com.shkurta.medication.domain.usecase.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTimelineUseCase: ObserveTimelineUseCase,
    private val markDoseTakenUseCase: MarkDoseTakenUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    private val deleteDoseLogUseCase: DeleteDoseLogUseCase
) : ViewModel() {

    val state: StateFlow<TimelineState> = observeTimelineUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimelineState(upcoming = emptyList(), history = emptyList())
        )

    fun markTaken(medicationId: Long) {
        viewModelScope.launch { markDoseTakenUseCase(medicationId) }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch { deleteMedicationUseCase(id) }
    }

    fun deleteDoseLog(id: Long) {
        viewModelScope.launch { deleteDoseLogUseCase(id) }
    }
}