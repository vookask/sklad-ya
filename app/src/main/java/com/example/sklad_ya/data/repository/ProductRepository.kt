package com.example.sklad_ya.data.repository

import com.example.sklad_ya.data.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с товарами
 */
interface ProductRepository {
    /**
     * Получить все товары
     */
    fun getAllProducts(): Flow<List<Product>>

    /**
     * Получить товар по ID
     */
    suspend fun getProductById(productId: String): Product?

    /**
     * Добавить товар
     */
    suspend fun addProduct(product: Product)

    /**
     * Обновить товар
     */
    suspend fun updateProduct(product: Product)

    /**
     * Удалить товар
     */
    suspend fun deleteProduct(productId: String)

    /**
     * Очистить все товары
     */
    suspend fun clearAllProducts()

    /**
     * Поиск товаров по запросу
     */
    fun searchProducts(query: String): Flow<List<Product>>

    /**
     * Получить товары по статусу
     */
    fun getProductsByStatus(status: com.example.sklad_ya.data.model.ProductStatus): Flow<List<Product>>
}