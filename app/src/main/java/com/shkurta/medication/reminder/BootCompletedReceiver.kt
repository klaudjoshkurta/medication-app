package com.shkurta.medication.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shkurta.medication.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val leadMs = TimeUnit.MINUTES.toMillis(5)
                repository.getPendingUpcoming().forEach { upcoming ->
                    val triggerAt = (upcoming.scheduledAt - leadMs).coerceAtLeast(
                        System.currentTimeMillis()
                    )
                    alarmScheduler.schedule(upcoming.medicationId, triggerAt)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
