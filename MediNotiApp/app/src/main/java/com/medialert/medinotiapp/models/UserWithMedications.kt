package com.medialert.medinotiapp.models

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithMedications(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val medications: List<Medication>
)
