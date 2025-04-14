package com.medialert.medinotiapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notebookId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
