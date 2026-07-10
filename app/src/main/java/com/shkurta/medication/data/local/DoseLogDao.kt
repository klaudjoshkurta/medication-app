package com.shkurta.medication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.shkurta.medication.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Insert
    suspend fun insert(log: DoseLogEntity): Long

    @Update
    suspend fun update(log: DoseLogEntity)

    @Query("DELETE FROM dose_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getById(id: Long): DoseLogEntity?

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC LIMIT 1")
    suspend fun getLatestForMedication(medicationId: Long): DoseLogEntity?

    @Query(
        """
        SELECT dl.id AS id,
               dl.medicationId AS medicationId,
               m.name AS medicationName,
               dl.takenAt AS takenAt,
               dl.nextDoseAt AS nextDoseAt
        FROM dose_logs dl
        INNER JOIN medications m ON m.id = dl.medicationId
        ORDER BY dl.takenAt DESC
        """
    )
    fun observeHistory(): Flow<List<DoseLogWithName>>

    @Query(
        """
        SELECT dl.medicationId AS medicationId,
               m.name AS medicationName,
               dl.nextDoseAt AS nextDoseAt
        FROM dose_logs dl
        INNER JOIN medications m ON m.id = dl.medicationId
        WHERE dl.nextDoseAt IS NOT NULL
          AND dl.nextDoseAt > :nowMillis
          AND dl.id = (
              SELECT id FROM dose_logs
              WHERE medicationId = dl.medicationId
              ORDER BY takenAt DESC
              LIMIT 1
          )
        ORDER BY dl.nextDoseAt ASC
        """
    )
    fun observeUpcoming(nowMillis: Long): Flow<List<UpcomingDoseRow>>

    @Query(
        """
        SELECT dl.medicationId AS medicationId,
               m.name AS medicationName,
               dl.nextDoseAt AS nextDoseAt
        FROM dose_logs dl
        INNER JOIN medications m ON m.id = dl.medicationId
        WHERE dl.nextDoseAt IS NOT NULL
          AND dl.nextDoseAt > :nowMillis
          AND dl.id = (
              SELECT id FROM dose_logs
              WHERE medicationId = dl.medicationId
              ORDER BY takenAt DESC
              LIMIT 1
          )
        """
    )
    suspend fun getUpcomingSnapshot(nowMillis: Long): List<UpcomingDoseRow>
}
