package com.shkurta.medication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shkurta.medication.domain.model.DoseLog
import com.shkurta.medication.domain.usecase.DeleteDoseLogUseCase
import com.shkurta.medication.domain.usecase.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeTimelineUseCase: ObserveTimelineUseCase,
    private val deleteDoseLogUseCase: DeleteDoseLogUseCase
) : ViewModel() {

    val history: StateFlow<List<DoseLog>> = observeTimelineUseCase()
        .map { it.history }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteDoseLog(id: Long) {
        viewModelScope.launch { deleteDoseLogUseCase(id) }
    }
}