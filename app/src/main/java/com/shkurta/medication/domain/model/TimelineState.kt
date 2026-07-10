package com.shkurta.medication.domain.model

data class TimelineState(
    val upcoming: List<UpcomingDose>,
    val history: List<DoseLog>
)
