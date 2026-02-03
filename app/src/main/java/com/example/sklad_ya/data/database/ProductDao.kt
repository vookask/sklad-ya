package com.example.sklad_ya.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с товарами в базе данных
 */
@Dao
interface ProductDao {
    /**
     * Получить все товары, отсортированные по индексу строки
     */
    @Query("SELECT * FROM products ORDER BY rowIndex ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    /**
     * Получить товар по productId
     */
    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?
    
    /**
     * Вставить один товар
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long
    
    /**
     * Вставить список товаров (для массовой загрузки)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    /**
     * Обновить товар (по первичному ключу id - не работает для наших задач!)
     * Вместо этого используем updateProductByProductId
     */
    @Update
    suspend fun updateProduct(product: ProductEntity)

    /**
     * Обновить товар по productId (правильный метод для обновления)
     */
    @Query("""
        UPDATE products SET
            article = :article,
            name = :name,
            barcode = :barcode,
            requiredQuantity = :requiredQuantity,
            actualQuantity = :actualQuantity,
            status = :status,
            storageCellsJson = :storageCellsJson,
            unit = :unit,
            comments = :comments,
            comment = :comment,
            fileStockQuantity = :fileStockQuantity,
            rowIndex = :rowIndex,
            originalDataJson = :originalDataJson,
            updatedAt = :updatedAt
        WHERE productId = :productId
    """)
    suspend fun updateProductByProductId(
        productId: String,
        article: String,
        name: String,
        barcode: String,
        requiredQuantity: Double,
        actualQuantity: Double,
        status: String,
        storageCellsJson: String,
        unit: String,
        comments: String,
        comment: String,
        fileStockQuantity: Double,
        rowIndex: Int,
        originalDataJson: String,
        updatedAt: Long
    )
    
    /**
     * Удалить товар по productId
     */
    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)
    
    /**
     * Удалить все товары
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    /**
     * Обновить количество и статус товара
     */
    @Query("UPDATE products SET actualQuantity = :quantity, status = :status, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun updateQuantityAndStatus(
        productId: String,
        quantity: Double,
        status: String,
        timestamp: Long
    )
    
    /**
     * Обновить ячейки хранения товара
     */
    @Query("UPDATE products SET storageCellsJson = :cellsJson, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun updateStorageCells(
        productId: String,
        cellsJson: String,
        timestamp: Long
    )
    
    /**
     * Обновить комментарии товара
     */
    @Query("UPDATE products SET comments = :comments, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun updateComments(
        productId: String,
        comments: String,
        timestamp: Long
    )
    
    /**
     * Получить товары по статусу
     */
    @Query("SELECT * FROM products WHERE status = :status ORDER BY rowIndex ASC")
    fun getProductsByStatus(status: String): Flow<List<ProductEntity>>
    
    /**
     * Получить количество товаров по статусу
     */
    @Query("SELECT COUNT(*) FROM products WHERE status = :status")
    suspend fun getCountByStatus(status: String): Int
}
