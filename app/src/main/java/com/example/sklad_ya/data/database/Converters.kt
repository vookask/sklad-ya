package com.example.sklad_ya.data.database

import androidx.room.TypeConverter
import com.example.sklad_ya.data.model.ProductStatus
import com.example.sklad_ya.data.model.StorageCell
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type Converters для Room Database
 * Использует Gson для сериализации/десериализации сложных объектов
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Конвертирует List<StorageCell> в JSON строку
     */
    @TypeConverter
    fun fromStorageCellList(cells: List<StorageCell>): String {
        return gson.toJson(cells)
    }
    
    /**
     * Конвертирует JSON строку в List<StorageCell>
     */
    @TypeConverter
    fun toStorageCellList(json: String): List<StorageCell> {
        val type = object : TypeToken<List<StorageCell>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Конвертирует ProductStatus в строку (имя enum)
     */
    @TypeConverter
    fun fromProductStatus(status: ProductStatus): String {
        return status.name
    }
    
    /**
     * Конвертирует строку в ProductStatus
     */
    @TypeConverter
    fun toProductStatus(name: String): ProductStatus {
        return try {
            ProductStatus.valueOf(name)
        } catch (e: IllegalArgumentException) {
            ProductStatus.PENDING // Значение по умолчанию при ошибке
        }
    }
    
    /**
     * Конвертирует Map<String, String> в JSON строку
     * Используется для originalData
     */
    @TypeConverter
    fun fromOriginalData(data: Map<String, String>): String {
        return gson.toJson(data)
    }
    
    /**
     * Конвертирует JSON строку в Map<String, String>
     */
    @TypeConverter
    fun toOriginalData(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type)
    }
}
