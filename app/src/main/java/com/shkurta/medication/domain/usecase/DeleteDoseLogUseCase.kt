package com.shkurta.medication.domain.usecase

import com.shkurta.medication.domain.repository.MedicationRepository
import javax.inject.Inject

class DeleteDoseLogUseCase @Inject constructor(
    private val repository: MedicationRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteDoseLog(id)
}
