package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.Medication
import com.medialert.medinotiapp.models.Take
import com.medialert.medinotiapp.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE correo = :correo")
    suspend fun getUserByCorreo(correo: String): User?

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
}