package com.shkurta.medication.data.repository

import com.shkurta.medication.data.local.DoseLogDao
import com.shkurta.medication.data.local.MedicationDao
import com.shkurta.medication.data.local.entity.DoseLogEntity
import com.shkurta.medication.data.local.entity.MedicationEntity
import com.shkurta.medication.domain.model.DoseLog
import com.shkurta.medication.domain.model.Medication
import com.shkurta.medication.domain.model.TimelineState
import com.shkurta.medication.domain.model.UpcomingDose
import com.shkurta.medication.domain.repository.MedicationRepository
import com.shkurta.medication.reminder.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val REMINDER_LEAD_MS = 5L * 60L * 1000L

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao,
    private val doseLogDao: DoseLogDao,
    private val alarmScheduler: AlarmScheduler
) : MedicationRepository {

    override fun observeTimeline(): Flow<TimelineState> {
        val now = System.currentTimeMillis()
        return doseLogDao.observeUpcoming(now).combine(doseLogDao.observeHistory()) { up, hist ->
            TimelineState(
                upcoming = up.map { UpcomingDose(it.medicationId, it.medicationName, it.nextDoseAt) },
                history = hist.map {
                    DoseLog(
                        id = it.id,
                        medicationId = it.medicationId,
                        medicationName = it.medicationName,
                        takenAt = it.takenAt,
                        nextDoseAt = it.nextDoseAt
                    )
                }
            )
        }
    }

    override suspend fun getMedication(id: Long): Medication? {
        val entity = medicationDao.getById(id) ?: return null
        return Medication(id = entity.id, name = entity.name, intervalHours = entity.intervalHours)
    }

    override suspend fun addMedication(name: String, intervalHours: Int?): Long {
        val now = System.currentTimeMillis()
        val medId = medicationDao.insert(
            MedicationEntity(name = name, intervalHours = intervalHours, createdAt = now)
        )
        val nextAt = intervalHours?.let { now + TimeUnit.HOURS.toMillis(it.toLong()) }
        doseLogDao.insert(
            DoseLogEntity(medicationId = medId, takenAt = now, nextDoseAt = nextAt)
        )
        if (nextAt != null) {
            alarmScheduler.schedule(medId, nextAt - REMINDER_LEAD_MS)
        }
        return medId
    }

    override suspend fun updateMedication(id: Long, name: String, intervalHours: Int?) {
        val existing = medicationDao.getById(id) ?: return
        medicationDao.update(existing.copy(name = name, intervalHours = intervalHours))

        val latest = doseLogDao.getLatestForMedication(id) ?: return
        val newNextAt = intervalHours?.let { latest.takenAt + TimeUnit.HOURS.toMillis(it.toLong()) }
        doseLogDao.update(latest.copy(nextDoseAt = newNextAt))

        alarmScheduler.cancel(id)
        if (newNextAt != null && newNextAt > System.currentTimeMillis()) {
            alarmScheduler.schedule(id, newNextAt - REMINDER_LEAD_MS)
        }
    }

    override suspend fun deleteMedication(id: Long) {
        alarmScheduler.cancel(id)
        medicationDao.deleteById(id)
    }

    override suspend fun deleteDoseLog(id: Long) {
        val log = doseLogDao.getById(id) ?: return
        doseLogDao.deleteById(id)

        val newLatest = doseLogDao.getLatestForMedication(log.medicationId)
        alarmScheduler.cancel(log.medicationId)
        val nextAt = newLatest?.nextDoseAt
        if (nextAt != null && nextAt > System.currentTimeMillis()) {
            alarmScheduler.schedule(log.medicationId, nextAt - REMINDER_LEAD_MS)
        }
    }

    override suspend fun markTaken(medicationId: Long) {
        val med = medicationDao.getById(medicationId) ?: return
        val now = System.currentTimeMillis()
        val nextAt = med.intervalHours?.let { now + TimeUnit.HOURS.toMillis(it.toLong()) }
        doseLogDao.insert(
            DoseLogEntity(medicationId = medicationId, takenAt = now, nextDoseAt = nextAt)
        )
        alarmScheduler.cancel(medicationId)
        if (nextAt != null) {
            alarmScheduler.schedule(medicationId, nextAt - REMINDER_LEAD_MS)
        }
    }

    override suspend fun snooze(medicationId: Long, snoozeMinutes: Int) {
        val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong())
        alarmScheduler.schedule(medicationId, triggerAt)
    }

    override suspend fun getPendingUpcoming(): List<UpcomingDose> {
        val now = System.currentTimeMillis()
        return doseLogDao.getUpcomingSnapshot(now)
            .map { UpcomingDose(it.medicationId, it.medicationName, it.nextDoseAt) }
    }
}
