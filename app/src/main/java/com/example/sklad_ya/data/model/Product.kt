package com.example.sklad_ya.data.model

/**
 * Модель товара для приёмки в ячейки хранения
 */
data class Product(
    val id: String = generateId(),              // Уникальный идентификатор
    val article: String = "",                   // Артикул
    val name: String = "",                      // Наименование товара
    val barcode: String = "",                   // Штрихкод
    val requiredQuantity: Double = 0.0,        // Требуемое количество
    val actualQuantity: Double = 0.0,          // Фактическое количество
    val status: ProductStatus = ProductStatus.PENDING, // Статус приёмки
    val storageCells: List<StorageCell> = emptyList(), // Ячейки хранения
    val unit: String = "",                      // Единица измерения
    val comments: String = "",                  // Комментарии
    val rowIndex: Int = 0,                      // Индекс строки в исходном файле
    val originalData: Map<String, String> = emptyMap(), // Оригинальные данные из Excel
    val fileStockQuantity: Double = 0.0,        // Остаток из файла Excel
    val comment: String = ""                       // Комментарий к товару
) {
    /**
     * Обновить фактическое количество и пересчитать статус
     */
    fun updateActualQuantity(quantity: Double): Product {
        val newStatus = when {
            quantity == 0.0 -> ProductStatus.PENDING
            quantity == requiredQuantity -> ProductStatus.MATCH
            else -> ProductStatus.MISMATCH
        }

        return copy(
            actualQuantity = quantity,
            status = newStatus
        )
    }

    /**
     * Обновить комментарии
     */
    fun updateComments(newComments: String): Product {
        return copy(comments = newComments)
    }

    /**
     * Добавить ячейку хранения
     */
    fun addStorageCell(cell: StorageCell): Product {
        val updatedCells = if (storageCells.any { it.toDisplayString() == cell.toDisplayString() }) {
            storageCells // Ячейка уже существует
        } else {
            storageCells + cell
        }
        return copy(storageCells = updatedCells)
    }

    /**
     * Удалить ячейку хранения
     */
    fun removeStorageCell(cellString: String): Product {
        val updatedCells = storageCells.filterNot { it.toDisplayString() == cellString }
        return copy(storageCells = updatedCells)
    }

    /**
     * Получить отформатированное количество для отображения
     */
    fun getFormattedQuantity(): String {
        return if (requiredQuantity % 1.0 == 0.0) {
            requiredQuantity.toInt().toString()
        } else {
            requiredQuantity.toString()
        }
    }

    /**
     * Получить отформатированное фактическое количество для отображения
     */
    fun getFormattedActualQuantity(): String {
        return if (actualQuantity == 0.0) {
            "" // Пустая строка вместо "0"
        } else if (actualQuantity % 1.0 == 0.0) {
            actualQuantity.toInt().toString()
        } else {
            actualQuantity.toString()
        }
    }

    /**
     * Получить объединённую строку ячеек хранения для отображения
     */
    fun getStorageCellsDisplayString(): String {
        return if (storageCells.isEmpty()) {
            ""
        } else {
            storageCells.joinToString(", ") { it.toDisplayString() }
        }
    }

    /**
     * Обновить комментарий к товару
     */
    fun updateComment(comment: String): Product {
        return copy(comment = comment)
    }

    /**
     * Валидировать данные товара
     * @return Result<Product> - успех если всё валидно, иначе список ошибок
     */
    fun validate(): Result<Product> {
        val errors = mutableListOf<String>()

        // CRITICAL: Артикул не может быть пустым
        if (article.isBlank()) {
            errors += "Артикул не может быть пустым"
        }

        // CRITICAL: Требуемое количество не может быть отрицательным
        if (requiredQuantity < 0) {
            errors += "Требуемое количество не может быть отрицательным (текущее: $requiredQuantity)"
        }

        // WARNING: Фактическое количество не может быть отрицательным
        if (actualQuantity < 0) {
            errors += "Фактическое количество не может быть отрицательным (текущее: $actualQuantity)"
        }

        // WARNING: Название товара желательно заполнить
        if (name.isBlank()) {
            errors += "Название товара не указано"
        }

        return if (errors.isEmpty()) {
            Result.success(this)
        } else {
            Result.failure(IllegalArgumentException(errors.joinToString("; ")))
        }
    }

    companion object {
        private val idCounter = java.util.concurrent.atomic.AtomicInteger(0)
        private fun generateId(): String {
            return "product_${idCounter.getAndIncrement()}"
        }
    }
}