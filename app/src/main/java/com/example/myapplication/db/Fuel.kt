package com.example.myapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel")
data class Fuel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fuel_type: String,
    val operation_type: String,
    val quantity_litr: String,
    val operatorID: Int,
    val plomba: String,
    val created_at: String
) 