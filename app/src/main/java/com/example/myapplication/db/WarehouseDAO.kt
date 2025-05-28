package com.example.myapplication.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WarehouseDAO {
    @Query("SELECT * FROM warehouses")
    fun getAll(): List<Warehouse>

    @Insert
    fun insertAll(vararg warehouse: Warehouse)

    @Query("SELECT * FROM warehouses WHERE warehouse_number = :warehouseNumber")
    fun getWarehouseByNumber(warehouseNumber: String): Warehouse?
} 