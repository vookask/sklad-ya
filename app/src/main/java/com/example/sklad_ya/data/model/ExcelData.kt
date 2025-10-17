package com.example.sklad_ya.data.model

/**
 * Данные, загруженные из Excel файла
 */
data class ExcelData(
    val fileName: String,
    val sheetName: String,
    val headers: List<String>,
    val products: List<Product>,
    val originalRowIndex: Int = 0, // Индекс строки с заголовками в оригинальном файле
    val columnMapping: List<Int> = emptyList(), // Маппинг колонок для восстановления
    val loadTime: Long = System.currentTimeMillis()
)

/**
 * Результат анализа таблицы Excel
 */
data class TableAnalysisResult(
    val headerRowIndex: Int,
    val headers: List<String>,
    val rows: List<List<String>>,
    val originalAoa: List<List<String>>,
    val columnMapping: List<Int>
)

/**
 * Состояние загрузки файла
 */
sealed class FileLoadState {
    object Idle : FileLoadState()
    object Loading : FileLoadState()
    data class Success(val data: ExcelData) : FileLoadState()
    data class Error(val message: String) : FileLoadState()
}