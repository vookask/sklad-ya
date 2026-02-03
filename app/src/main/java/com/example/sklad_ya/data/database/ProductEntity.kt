package com.example.sklad_ya.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения товаров в локальной базе данных
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val productId: String,           // Уникальный ID из Product.id
    
    val article: String,
    val name: String,
    val barcode: String,
    val requiredQuantity: Double,
    val actualQuantity: Double,
    val status: String,             // ProductStatus.name()
    val storageCellsJson: String,    // JSON сериализация List<StorageCell>
    val unit: String,
    val price: Double,
    val comments: String,
    val comment: String,
    val fileStockQuantity: Double,
    val rowIndex: Int,
    val originalDataJson: String,     // JSON сериализация Map<String, String>
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
