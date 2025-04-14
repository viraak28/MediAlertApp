package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Int): Medication

    @Query("SELECT * FROM medications WHERE user_id = :userId")
    fun getMedicationsByUser(userId: Int): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(medication: Medication)

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTake(take: Take)

    @Query("SELECT * FROM takes WHERE medicationId = :medicationId")
    fun getTakesForMedication(medicationId: Int): Flow<List<Take>>
}
