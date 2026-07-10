package com.shkurta.medication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shkurta.medication.data.local.entity.MedicationEntity

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications WHERE active = 1")
    suspend fun getAllActive(): List<MedicationEntity>
}
