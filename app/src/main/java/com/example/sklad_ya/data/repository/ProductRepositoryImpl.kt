package com.example.sklad_ya.data.repository

import android.content.Context
import com.example.sklad_ya.data.database.ProductDao
import com.example.sklad_ya.data.database.toDomainModel
import com.example.sklad_ya.data.database.toEntity
import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация репозитория товаров с использованием Room Database
 */
class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val context: Context
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun getProductById(productId: String): Product? {
        return productDao.getProductById(productId)?.toDomainModel()
    }

    override suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
    }

    override suspend fun clearAllProducts() {
        productDao.deleteAllProducts()
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return getAllProducts().map { products ->
            if (query.isBlank()) {
                products
            } else {
                products.filter { product ->
                    product.article.contains(query, ignoreCase = true) ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true) ||
                    product.getStorageCellsDisplayString().contains(query, ignoreCase = true) ||
                    product.unit.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override fun getProductsByStatus(status: ProductStatus): Flow<List<Product>> {
        return getAllProducts().map { products ->
            products.filter { it.status == status }
        }
    }
}
