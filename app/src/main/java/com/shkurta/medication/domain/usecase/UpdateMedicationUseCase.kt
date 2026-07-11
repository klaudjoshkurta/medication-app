package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.repository.MedicationRepository
import javax.inject.Inject

class UpdateMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(
        id: Long,
        name: String,
        cause: String?,
        description: String?,
        intervalHours: Int?
    ) = repository.updateMedication(
        id,
        name.trim(),
        cause?.trim(),
        description?.trim(),
        intervalHours
    )
}
