package com.shkurta.medication.di

import android.content.Context
import androidx.room.Room
import com.shkurta.medication.data.local.DoseLogDao
import com.shkurta.medication.data.local.MedicationDao
import com.shkurta.medication.data.local.MedicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedicationDatabase =
        Room.databaseBuilder(context, MedicationDatabase::class.java, "medication.db")
            .addMigrations(MedicationDatabase.MIGRATION_1_2, MedicationDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideMedicationDao(db: MedicationDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideDoseLogDao(db: MedicationDatabase): DoseLogDao = db.doseLogDao()
}
