package com.medialert.medinotiapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var dosage: String,
    var frequency: String
)
