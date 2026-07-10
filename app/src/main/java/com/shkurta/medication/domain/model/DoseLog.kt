package com.shkurta.medication.domain.model

data class DoseLog(
    val id: Long,
    val medicationId: Long,
    val medicationName: String,
    val takenAt: Long,
    val nextDoseAt: Long?
)
