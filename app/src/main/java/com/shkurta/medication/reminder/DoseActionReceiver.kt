package com.shkurta.medication.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shkurta.medication.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        if (medId < 0) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_TAKEN -> {
                        repository.markTaken(medId)
                        NotificationHelper.dismiss(context, medId)
                    }
                    ACTION_SNOOZE -> {
                        repository.snooze(medId)
                        NotificationHelper.dismiss(context, medId)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.shkurta.medication.action.MARK_TAKEN"
        const val ACTION_SNOOZE = "com.shkurta.medication.action.SNOOZE"
        const val EXTRA_MED_ID = "extra_med_id"
    }
}
