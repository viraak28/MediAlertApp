package com.medialert.medinotiapp.data

import androidx.room.*
import com.medialert.medinotiapp.models.UserWithMedications
import com.medialert.medinotiapp.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE nombre = :nombre")
    suspend fun getUserByUsername(nombre: String): User?

    @Query("SELECT * FROM users WHERE correo = :correo")
    suspend fun getUserByCorreo(correo: String): User?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithMedications(userId: Int): UserWithMedications
}