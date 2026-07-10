package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.repository.MedicationRepository
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteMedication(id)
}
