package com.shkurta.medication.ui.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shkurta.medication.domain.usecase.MarkDoseTakenUseCase
import com.shkurta.medication.domain.usecase.SnoozeDoseUseCase
import com.shkurta.medication.reminder.NotificationHelper
import com.shkurta.medication.ui.theme.MedicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenAlarmActivity : ComponentActivity() {

    @Inject lateinit var markDoseTakenUseCase: MarkDoseTakenUseCase
    @Inject lateinit var snoozeDoseUseCase: SnoozeDoseUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val medId = intent.getLongExtra(EXTRA_MED_ID, -1L)
        val medName = intent.getStringExtra(EXTRA_MED_NAME) ?: "Medication"

        setContent {
            MedicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmContent(
                        medicationName = medName,
                        onTaken = {
                            if (medId >= 0) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    markDoseTakenUseCase(medId)
                                    NotificationHelper.dismiss(this@FullScreenAlarmActivity, medId)
                                }
                            }
                            finish()
                        },
                        onSnooze = {
                            if (medId >= 0) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    snoozeDoseUseCase(medId)
                                    NotificationHelper.dismiss(this@FullScreenAlarmActivity, medId)
                                }
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
    }
}

@Composable
private fun AlarmContent(
    medicationName: String,
    onTaken: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time to take",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = medicationName,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your next dose is due in 5 minutes.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = onTaken) {
            Text("Mark as taken")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSnooze) {
            Text("Snooze 5 minutes")
        }
    }
}
