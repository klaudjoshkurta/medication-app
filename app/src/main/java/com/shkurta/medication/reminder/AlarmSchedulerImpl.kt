package com.shkurta.medication.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(medicationId: Long, triggerAtMillis: Long) {
        val pi = reminderPendingIntent(medicationId)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    override fun cancel(medicationId: Long) {
        alarmManager.cancel(reminderPendingIntent(medicationId))
    }

    private fun reminderPendingIntent(medicationId: Long): PendingIntent {
        val intent = Intent(context, DoseReminderReceiver::class.java).apply {
            action = DoseReminderReceiver.ACTION_FIRE
            putExtra(DoseReminderReceiver.EXTRA_MED_ID, medicationId)
        }
        return PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
