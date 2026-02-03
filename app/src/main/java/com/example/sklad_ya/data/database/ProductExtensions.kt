package com.example.sklad_ya.data.database

import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.StorageCell
import com.example.sklad_ya.data.model.ProductStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Extension функции для конверсии между Entity и Domain моделями
 */

/**
 * Конвертирует ProductEntity в Product (Domain модель)
 */
fun ProductEntity.toDomainModel(): Product {
    val gson = Gson()
    
    val cells: List<StorageCell> = try {
        val type: Type = object : TypeToken<List<StorageCell>>() {}.type
        gson.fromJson(
            storageCellsJson,
            type
        )
    } catch (e: Exception) {
        emptyList()
    }
    
    val originalData: Map<String, String> = try {
        val type: Type = object : TypeToken<Map<String, String>>() {}.type
        gson.fromJson(
            originalDataJson,
            type
        )
    } catch (e: Exception) {
        emptyMap()
    }
    
    return Product(
        id = productId,
        article = article,
        name = name,
        barcode = barcode,
        requiredQuantity = requiredQuantity,
        actualQuantity = actualQuantity,
        status = try {
            // Парсим статус из базы данных, поддерживая символы "✓" и "⚠"
            when (status.trim()) {
                "PENDING" -> ProductStatus.PENDING
                "MATCH" -> ProductStatus.MATCH
                "MISMATCH" -> ProductStatus.MISMATCH
                "✓" -> ProductStatus.MATCH
                "⚠" -> ProductStatus.MISMATCH
                else -> {
                    // Если фактическое количество пустое, статус должен быть PENDING
                    if (actualQuantity == 0.0) {
                        ProductStatus.PENDING
                    } else {
                        // Иначе вычисляем статус на основе данных
                        when {
                            requiredQuantity > 0 && actualQuantity > 0 -> {
                                if (requiredQuantity == actualQuantity) {
                                    ProductStatus.MATCH
                                } else {
                                    ProductStatus.MISMATCH
                                }
                            }
                            requiredQuantity > 0 -> ProductStatus.PENDING
                            else -> ProductStatus.PENDING
                        }
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            ProductStatus.PENDING
        },
        storageCells = cells,
        unit = unit,
        price = price,
        comments = comments,
        comment = comment,
        rowIndex = rowIndex,
        originalData = originalData,
        fileStockQuantity = fileStockQuantity
    )
}

/**
 * Конвертирует Product (Domain модель) в ProductEntity
 */
fun Product.toEntity(): ProductEntity {
    val gson = Gson()
    
    return ProductEntity(
        productId = id,
        article = article,
        name = name,
        barcode = barcode,
        requiredQuantity = requiredQuantity,
        actualQuantity = actualQuantity,
        status = status.name,
        storageCellsJson = gson.toJson(storageCells),
        unit = unit,
        price = price,
        comments = comments,
        comment = comment,
        rowIndex = rowIndex,
        originalDataJson = gson.toJson(originalData),
        fileStockQuantity = fileStockQuantity,
        updatedAt = System.currentTimeMillis()
    )
}
