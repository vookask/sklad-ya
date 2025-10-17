package com.example.sklad_ya.data.service

import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus

/**
 * Сервис для поиска и фильтрации товаров
 */
interface SearchService {
    /**
     * Поиск товаров по запросу
     */
    fun searchProducts(products: List<Product>, query: String): List<Product>

    /**
     * Фильтрация товаров по статусу
     */
    fun filterProductsByStatus(products: List<Product>, status: ProductStatus): List<Product>

    /**
     * Фильтрация товаров по ячейкам хранения
     */
    fun filterProductsByStorageCells(products: List<Product>, cellQuery: String): List<Product>

    /**
     * Сортировка товаров
     */
    fun sortProducts(products: List<Product>, sortBy: SortCriteria): List<Product>
}

/**
 * Критерии сортировки товаров
 */
enum class SortCriteria {
    ARTICLE_ASC,      // По артикулу (возрастание)
    ARTICLE_DESC,     // По артикулу (убывание)
    NAME_ASC,         // По названию (возрастание)
    NAME_DESC,        // По названию (убывание)
    QUANTITY_ASC,     // По количеству (возрастание)
    QUANTITY_DESC,    // По количеству (убывание)
    STATUS           // По статусу
}

/**
 * Реализация сервиса поиска
 */
class SearchServiceImpl : SearchService {
    override fun searchProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products

        val lowerQuery = query.lowercase()

        return products.filter { product ->
            product.article.lowercase().contains(lowerQuery) ||
            product.name.lowercase().contains(lowerQuery) ||
            product.barcode.lowercase().contains(lowerQuery) ||
            product.getStorageCellsDisplayString().lowercase().contains(lowerQuery)
        }
    }

    override fun filterProductsByStatus(products: List<Product>, status: ProductStatus): List<Product> {
        return products.filter { it.status == status }
    }

    override fun filterProductsByStorageCells(products: List<Product>, cellQuery: String): List<Product> {
        if (cellQuery.isBlank()) return products

        return products.filter { product ->
            product.getStorageCellsDisplayString().contains(cellQuery, ignoreCase = true)
        }
    }

    override fun sortProducts(products: List<Product>, sortBy: SortCriteria): List<Product> {
        return when (sortBy) {
            SortCriteria.ARTICLE_ASC -> products.sortedBy { it.article }
            SortCriteria.ARTICLE_DESC -> products.sortedByDescending { it.article }
            SortCriteria.NAME_ASC -> products.sortedBy { it.name }
            SortCriteria.NAME_DESC -> products.sortedByDescending { it.name }
            SortCriteria.QUANTITY_ASC -> products.sortedBy { it.requiredQuantity }
            SortCriteria.QUANTITY_DESC -> products.sortedByDescending { it.requiredQuantity }
            SortCriteria.STATUS -> products.sortedBy { it.status.ordinal }
        }
    }
}