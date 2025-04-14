package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes WHERE notebookId = :notebookId AND timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getNotesByDateRange(notebookId: Long, start: Long, end: Long): List<Note>

    @Query("SELECT EXISTS(SELECT 1 FROM notes WHERE notebookId = :notebookId AND timestamp < :beforeTimestamp LIMIT 1)")
    suspend fun hasNotesBefore(notebookId: Long, beforeTimestamp: Long): Boolean

    @Delete
    suspend fun delete(note: Note)
}