package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.Notebook
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Insert
    suspend fun insert(notebook: Notebook): Long

    @Query("SELECT * FROM notebooks WHERE userId = :userId ORDER BY createdDate DESC")
    fun getNotebooksByUser(userId: Int): Flow<List<Notebook>>

    @Delete
    suspend fun delete(notebook: Notebook)
}
