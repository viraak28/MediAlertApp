package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications")
    fun getAll(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Int): Medication

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
