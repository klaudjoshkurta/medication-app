package com.shkurta.medication.reminder

interface AlarmScheduler {
    fun schedule(medicationId: Long, triggerAtMillis: Long)
    fun cancel(medicationId: Long)
}
