package com.medialert.medinotiapp.models

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithNotebooks(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val notebooks: List<Notebook>
)
