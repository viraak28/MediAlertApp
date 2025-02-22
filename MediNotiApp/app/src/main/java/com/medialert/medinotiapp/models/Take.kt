package com.medialert.medinotiapp.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "takes",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicationId")]
)
data class Take(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
