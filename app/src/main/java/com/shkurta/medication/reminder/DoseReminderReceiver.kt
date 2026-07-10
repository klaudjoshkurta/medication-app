package com.shkurta.medication.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shkurta.medication.data.local.MedicationDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DoseReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var medicationDao: MedicationDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        if (medId < 0) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val med = medicationDao.getById(medId) ?: return@launch
                val notif = NotificationHelper.buildDoseReminder(context, medId, med.name)
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NotificationHelper.notificationId(medId), notif)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.shkurta.medication.action.DOSE_REMINDER"
        const val EXTRA_MED_ID = "extra_med_id"
    }
}
