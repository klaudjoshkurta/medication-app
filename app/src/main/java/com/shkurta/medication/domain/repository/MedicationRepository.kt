package com.shkurta.medication.domain.repository

import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.model.UpcomingDose
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeTimeline(): Flow<TimelineState>

    suspend fun addMedication(name: String, intervalHours: Int?): Long

    suspend fun markTaken(medicationId: Long)

    suspend fun snooze(medicationId: Long, snoozeMinutes: Int = 5)

    suspend fun getPendingUpcoming(): List<UpcomingDose>
}
