package com.medialert.medinotiapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medialert.medinotiapp.database.Converters
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import com.medialert.medinotiapp.models.User

@TypeConverters(Converters::class)
@Database(entities = [User::class, Medication::class, Take::class], version = 1, exportSchema = false)
abstract class MedinotiappDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: MedinotiappDatabase? = null

        fun getDatabase(context: Context): MedinotiappDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedinotiappDatabase::class.java,
                    "medinotiapp_database"
                ).addTypeConverter(Converters())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
