package com.shkurta.medication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shkurta.medication.data.local.entity.DoseLogEntity
import com.shkurta.medication.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class, DoseLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MedicationDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN cause TEXT")
                db.execSQL("ALTER TABLE medications ADD COLUMN description TEXT")
            }
        }
    }
}
