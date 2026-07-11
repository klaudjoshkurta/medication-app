package com.shkurta.medication.domain.repository

import com.shkurta.medication.domain.model.Medication
import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.model.UpcomingDose
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeTimeline(): Flow<TimelineState>

    suspend fun getMedication(id: Long): Medication?

    suspend fun addMedication(
        name: String,
        cause: String?,
        description: String?,
        intervalHours: Int?,
        takenAtMillis: Long
    ): Long

    suspend fun updateMedication(
        id: Long,
        name: String,
        cause: String?,
        description: String?,
        intervalHours: Int?
    )

    suspend fun deleteMedication(id: Long)

    suspend fun deleteDoseLog(id: Long)

    suspend fun markTaken(medicationId: Long)

    suspend fun snooze(medicationId: Long, snoozeMinutes: Int = 5)

    suspend fun getPendingUpcoming(): List<UpcomingDose>
}
