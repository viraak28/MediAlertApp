package com.medialert.medinotiapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var dosage: String,
    var dosageQuantity: String,
    var administrationType: String,
    var frecuencyOfTakeMedicine: String,
    var frecuencyOfTakeMedicineExactDay: String,
    var frequency: String,
    var breakfast: Boolean,
    var midMorning: Boolean,
    var lunch: Boolean,
    var snacking: Boolean,
    var dinner: Boolean,
    @ColumnInfo(name = "user_id") val userId: Int
)
