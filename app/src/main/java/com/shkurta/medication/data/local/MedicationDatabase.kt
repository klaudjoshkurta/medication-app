package com.shkurta.medication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shkurta.medication.data.local.entity.DoseLogEntity
import com.shkurta.medication.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class, DoseLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
}
