package com.shkurta.medication.data.local

data class DoseLogWithName(
    val id: Long,
    val medicationId: Long,
    val medicationName: String,
    val medicationCause: String?,
    val medicationDescription: String?,
    val takenAt: Long,
    val nextDoseAt: Long?
)

data class UpcomingDoseRow(
    val medicationId: Long,
    val medicationName: String,
    val medicationCause: String?,
    val medicationDescription: String?,
    val nextDoseAt: Long
)
