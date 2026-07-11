package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.repository.MedicationRepository
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(
        name: String,
        cause: String?,
        description: String?,
        intervalHours: Int?,
        takenAtMillis: Long
    ): Long = repository.addMedication(
        name.trim(),
        cause?.trim(),
        description?.trim(),
        intervalHours,
        takenAtMillis
    )
}
