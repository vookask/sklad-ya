package com.example.sklad_ya.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения ячеек хранения в локальной базе данных
 */
@Entity(tableName = "storage_cells")
data class StorageCellEntity(
    @PrimaryKey
    val cellString: String,    // "A1-1-1"
    val letter: Char,
    val number1: Int,
    val number2: Int,
    val number3: Int
)
