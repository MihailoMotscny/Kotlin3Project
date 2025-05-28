package com.example.myapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seeds")
data class Seeds(
    @PrimaryKey val id: Int,
    val variety: String,
    val quantity_kg: String,
    val warehouse_id: Int,
    val created_at: String
) 