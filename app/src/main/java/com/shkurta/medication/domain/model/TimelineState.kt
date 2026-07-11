package com.shkurta.medication.domain.model

data class TimelineState(
    val medications: List<Medication> = emptyList(),
    val upcoming: List<UpcomingDose>,
    val history: List<DoseLog>
)
