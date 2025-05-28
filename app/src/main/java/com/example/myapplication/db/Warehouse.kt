package com.example.myapplication.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val product_type: String,
    val operation_type: String,
    val quantity_kg: String,
    val issued_to: String,
    val warehouse_number: String,
    val operatorID: Int,
    val created_at: String
) 