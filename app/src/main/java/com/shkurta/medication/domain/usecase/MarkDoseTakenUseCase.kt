package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.repository.MedicationRepository
import javax.inject.Inject

class MarkDoseTakenUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(medicationId: Long) = repository.markTaken(medicationId)
}
