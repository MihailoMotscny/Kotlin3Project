package com.example.myapplication.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: Seeds)

    @Query("SELECT * FROM seeds")
    suspend fun getAll(): List<Seeds>
}

@Dao
interface UsersDAAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: Users)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<Users>

    @Query("SELECT * FROM users WHERE login = :login AND password = :password")
    suspend fun doUserIsCreated(login: String, password: String): Users?

    @Query("SELECT * FROM users WHERE login = :login")
    suspend fun getUserByLogin(login: String): Users?
}

@Dao
interface FuelDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fuel_perationss: Fuel)

    @Query("SELECT * FROM fuel")
    suspend fun getAll(): List<Fuel>
}


