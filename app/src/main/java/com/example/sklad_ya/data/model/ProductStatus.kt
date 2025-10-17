package com.example.sklad_ya.data.model

/**
 * Статус товара при приёмке
 */
enum class ProductStatus {
    PENDING,    // Ожидает проверки
    MATCH,      // Количество совпадает (✓)
    MISMATCH;   // Количество не совпадает (⚠)

    /**
     * Получить символ для отображения статуса
     */
    fun getSymbol(): String {
        return when (this) {
            PENDING -> ""
            MATCH -> "✓"
            MISMATCH -> "⚠"
        }
    }

    /**
     * Получить цвет для отображения статуса
     */
    fun getColor(): ProductStatusColor {
        return when (this) {
            PENDING -> ProductStatusColor.GRAY
            MATCH -> ProductStatusColor.GREEN
            MISMATCH -> ProductStatusColor.YELLOW
        }
    }
}

/**
 * Цвета для статусов товаров
 */
enum class ProductStatusColor {
    GRAY, GREEN, YELLOW
}