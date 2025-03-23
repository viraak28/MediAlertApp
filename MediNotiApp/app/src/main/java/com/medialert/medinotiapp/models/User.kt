package com.medialert.medinotiapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var nombre: String,
    var apellido: String,
    var correo: String,
    var contrasena: String
)
