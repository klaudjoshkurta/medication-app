package com.shkurta.medication.domain.model

data class UpcomingDose(
    val medicationId: Long,
    val medicationName: String,
    val scheduledAt: Long
)
