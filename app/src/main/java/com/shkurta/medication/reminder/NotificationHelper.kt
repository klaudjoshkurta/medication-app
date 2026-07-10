package com.shkurta.medication.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shkurta.medication.R
import com.shkurta.medication.ui.alarm.FullScreenAlarmActivity

object NotificationHelper {
    const val CHANNEL_ID = "dose_reminders"
    private const val CHANNEL_NAME = "Dose reminders"

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen reminders 5 minutes before each dose"
                enableVibration(true)
                setBypassDnd(true)
            }
            nm.createNotificationChannel(ch)
        }
    }

    fun buildDoseReminder(
        context: Context,
        medicationId: Long,
        medicationName: String
    ): android.app.Notification {
        ensureChannel(context)

        val fullScreen = Intent(context, FullScreenAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(FullScreenAlarmActivity.EXTRA_MED_ID, medicationId)
            putExtra(FullScreenAlarmActivity.EXTRA_MED_NAME, medicationName)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            fullScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_MARK_TAKEN
            putExtra(DoseActionReceiver.EXTRA_MED_ID, medicationId)
        }
        val takenPi = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_SNOOZE
            putExtra(DoseActionReceiver.EXTRA_MED_ID, medicationId)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            (medicationId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take $medicationName")
            .setContentText("Your next dose is due in 5 minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .setAutoCancel(true)
            .addAction(0, "Mark taken", takenPi)
            .addAction(0, "Snooze 5 min", snoozePi)
            .build()
    }

    fun notificationId(medicationId: Long): Int = medicationId.toInt()

    fun dismiss(context: Context, medicationId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notificationId(medicationId))
    }
}
