package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTimelineUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    operator fun invoke(): Flow<TimelineState> = repository.observeTimeline()
}
