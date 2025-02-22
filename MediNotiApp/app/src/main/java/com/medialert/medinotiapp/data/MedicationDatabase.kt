package com.medialert.medinotiapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medialert.medinotiapp.database.Converters
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take

@TypeConverters(Converters::class)
@Database(entities = [Medication::class, Take::class], version = 1, exportSchema = false)
abstract class MedicationDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: MedicationDatabase? = null

        fun getDatabase(context: Context): MedicationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicationDatabase::class.java,
                    "medication_database"
                ).addTypeConverter(Converters())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
