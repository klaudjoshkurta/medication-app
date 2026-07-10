package com.shkurta.medication.domain.model

data class Medication(
    val id: Long,
    val name: String,
    val intervalHours: Int?
)
