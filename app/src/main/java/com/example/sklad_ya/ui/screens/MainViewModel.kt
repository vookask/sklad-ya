package com.example.sklad_ya.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sklad_ya.data.model.ExcelData
import com.example.sklad_ya.data.model.FileLoadState
import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus
import com.example.sklad_ya.data.repository.ProductRepository
import com.example.sklad_ya.data.service.ExcelService
import com.example.sklad_ya.data.service.FileService
import com.example.sklad_ya.data.service.SearchService
import com.example.sklad_ya.data.service.StorageCellService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Состояние экспорта данных
 */
sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

/**
 * ViewModel главного экрана приложения
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val excelService: ExcelService,
    private val fileService: FileService,
    private val searchService: SearchService,
    private val storageCellService: StorageCellService
) : ViewModel() {

    companion object {
        private const val MAX_DEBUG_LOG_SIZE = 100
    }

    // Debug логи для отладки
    private val _debugLogs = MutableStateFlow<List<String>>(emptyList())
    val debugLogs: StateFlow<List<String>> = _debugLogs.asStateFlow()

    /**
     * Добавить сообщение в debug логи с ограничением размера
     */
    private fun addDebugLog(message: String) {
        val current = _debugLogs.value
        val updated = if (current.size >= MAX_DEBUG_LOG_SIZE) {
            // Удаляем самый старый лог
            current.drop(1) + message
        } else {
            current + message
        }
        _debugLogs.value = updated
    }

    // Состояние загрузки файла
    private val _fileLoadState = MutableStateFlow<FileLoadState>(FileLoadState.Idle)
    val fileLoadState: StateFlow<FileLoadState> = _fileLoadState.asStateFlow()

    // Список товаров
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Поисковый запрос
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Отфильтрованный список товаров
    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    // Состояние экспорта
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        android.util.Log.d("MainViewModel", "MainViewModel init started")
        // При инициализации загружаем сохранённые данные
        loadSavedData()
        android.util.Log.d("MainViewModel", "MainViewModel init completed")
    }

    /**
     * Загрузить данные из файла Excel
     */
    fun loadExcelFile(context: android.content.Context, fileUri: android.net.Uri) {
        viewModelScope.launch {
            _fileLoadState.value = FileLoadState.Loading

            try {
                // Проверяем, что URI не пустой и корректный
                if (fileUri.toString().isBlank()) {
                    _fileLoadState.value = FileLoadState.Error("Выбран пустой файл")
                    return@launch
                }

                // Проверяем расширение файла
                val fileName = fileService.getFileNameFromUri(context, fileUri) ?: ""
                if (!fileName.lowercase().endsWith(".xlsx") && !fileName.lowercase().endsWith(".xls")) {
                    _fileLoadState.value = FileLoadState.Error("Выберите файл Excel (.xlsx или .xls)")
                    return@launch
                }

                // Получаем путь к файлу из URI
                val filePathResult = fileService.getFilePathFromUri(context, fileUri)
                filePathResult.fold(
                    onSuccess = { filePath ->
                        // Загружаем данные из Excel файла
                        val result = excelService.loadExcelData(context, filePath)
                        result.fold(
                            onSuccess = { excelData ->
                                _products.value = excelData.products
                                _searchQuery.value = "" // Сбрасываем поисковый запрос при загрузке нового файла
                                _filteredProducts.value = excelData.products

                                // Логируем предупреждения если есть
                                if (excelData.warnings.isNotEmpty()) {
                                    val warningMessage = "Загружено ${excelData.products.size} товаров. " +
                                            "Пропущено ${excelData.warnings.size} некорректных.\n\n" +
                                            "Пропущенные:\n" + excelData.warnings.take(5).joinToString("\n") +
                                            if (excelData.warnings.size > 5) "\n... и ещё ${excelData.warnings.size - 5}" else ""
                                    android.util.Log.w("MainViewModel", "Предупреждения при загрузке: $warningMessage")
                                    // TODO: Можно показать warning в UI через Snackbar
                                }

                                _fileLoadState.value = FileLoadState.Success(excelData)
                            },
                            onFailure = { exception ->
                                _fileLoadState.value = FileLoadState.Error("Ошибка чтения Excel файла: ${exception.message}. Файл должен быть в формате .xlsx или .xls")
                            }
                        )
                    },
                    onFailure = { exception ->
                        _fileLoadState.value = FileLoadState.Error("Ошибка доступа к файлу: ${exception.message}. Убедитесь, что файл не поврежден и приложение имеет доступ к файлам.")
                    }
                )
            } catch (e: Exception) {
                _fileLoadState.value = FileLoadState.Error("Ошибка загрузки файла: ${e.message}")
            }
        }
    }

    /**
     * Загрузить тестовые данные для демонстрации
     */
    private fun loadTestData() {
        // Используем тестовые данные напрямую вместо загрузки из файла
        _products.value = listOf(
            Product(
                article = "ART001",
                name = "Смартфон Samsung Galaxy S21",
                barcode = "1234567890123",
                requiredQuantity = 10.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт"
            ),
            Product(
                article = "ART002",
                name = "Ноутбук ASUS VivoBook",
                barcode = "1234567890124",
                requiredQuantity = 5.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт"
            )
        )
        _filteredProducts.value = _products.value
    }

    /**
     * Обновить фактическое количество товара и сохранить в БД
     */
    fun updateProductQuantity(productId: String, quantity: Double) {
        val currentProducts = _products.value
        val logMessage = "updateProductQuantity: productId=$productId, quantity=$quantity, currentProducts.size=${currentProducts.size}"
        android.util.Log.d("DEBUG", logMessage)
        addDebugLog(logMessage)

        val updatedProducts = currentProducts.map { product ->
            if (product.id == productId) {
                val updated = product.updateActualQuantity(quantity)
                val updateLog = "updateProductQuantity: updated product ${product.id}, actualQuantity: ${product.actualQuantity} -> ${updated.actualQuantity}"
                android.util.Log.d("DEBUG", updateLog)
                addDebugLog(updateLog)
                updated
            } else {
                product
            }
        }
        _products.value = updatedProducts
        
        // Сохраняем через Repository
        val product = updatedProducts.find { it.id == productId }
        if (product != null) {
            viewModelScope.launch {
                productRepository.updateProduct(product)
            }
        }
        
        applySearchFilter()
    }

    /**
     * Обновить комментарии товара и сохранить в БД
     */
    fun updateProductComments(productId: String, comments: String) {
        val currentProducts = _products.value
        val updatedProducts = currentProducts.map { product ->
            if (product.id == productId) {
                product.updateComments(comments)
            } else {
                product
            }
        }
        _products.value = updatedProducts
        
        // Сохраняем через Repository
        val product = updatedProducts.find { it.id == productId }
        if (product != null) {
            viewModelScope.launch {
                productRepository.updateProduct(product)
            }
        }
        
        applySearchFilter()
    }

    /**
     * Добавить ячейку хранения для товара и сохранить в БД
     */
    fun addStorageCellToProduct(productId: String, cellString: String) {
        val currentProducts = _products.value
        val updatedProducts = currentProducts.map { product ->
            if (product.id == productId) {
                val cell = com.example.sklad_ya.data.model.StorageCell.fromString(cellString)
                if (cell != null) {
                    product.addStorageCell(cell)
                } else {
                    product
                }
            } else {
                product
            }
        }
        _products.value = updatedProducts
        
        // Сохраняем через Repository
        val product = updatedProducts.find { it.id == productId }
        if (product != null) {
            viewModelScope.launch {
                productRepository.updateProduct(product)
            }
        }
        
        applySearchFilter()
    }

    /**
     * Обновить поисковый запрос
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    /**
     * Очистить поиск
     */
    fun clearSearch() {
        _searchQuery.value = ""
        applySearchFilter()
    }

    /**
     * Экспортировать данные в Excel файл
     */
    fun exportToExcel(context: android.content.Context) {
        val products = _products.value
        if (products.isEmpty()) {
            _exportState.value = ExportState.Error("Нет данных для экспорта")
            return
        }

        _exportState.value = ExportState.Loading

        // Запускаем корутину для экспорта
        viewModelScope.launch {
            try {
                // Создаем уникальное имя файла с датой и временем
                val currentTime = java.time.LocalDateTime.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                val timestamp = currentTime.format(formatter)
                val fileName = "sklad_export_${timestamp}.xlsx"

                // Проверяем разрешения для Android 10+ используем scoped storage
                val filePath = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // Используем MediaStore для Android 10+
                    saveFileUsingMediaStore(context, fileName, products)
                } else {
                    // Для старых версий используем прямой доступ к файловой системе
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val file = java.io.File(downloadsDir, fileName)
                    val filePath = file.absolutePath

                    // Создаем ExcelData из текущих продуктов
                    val excelData = ExcelData(
                        fileName = fileName,
                        sheetName = "Товары",
                        headers = listOf("Артикул", "Товар", "Кол-во", "Факт", "Статус", "Ячейки"),
                        products = products
                    )

                    val result = excelService.saveExcelData(excelData, filePath)
                    result.fold(
                        onSuccess = { filePath },
                        onFailure = { throw it }
                    )
                }

                android.util.Log.d("EXPORT", "Файл успешно сохранен: $filePath")
                _exportState.value = ExportState.Success(filePath)

            } catch (e: Exception) {
                android.util.Log.e("EXPORT", "Ошибка экспорта: ${e.message}", e)
                _exportState.value = ExportState.Error("Ошибка экспорта: ${e.message}")
            }
        }
    }

    /**
     * Сохранение файла через MediaStore для Android 10+
     */
    @android.annotation.TargetApi(29)
    private suspend fun saveFileUsingMediaStore(context: android.content.Context, fileName: String, products: List<Product>): String {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Не удалось создать файл в MediaStore")

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                // Создаем Excel файл в памяти
                val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
                val sheet = workbook.createSheet("Товары")

                // Получаем данные для сохранения
                val dataRows = excelService.prepareDataForSaving(ExcelData(
                    fileName = fileName,
                    sheetName = "Товары",
                    headers = listOf("Артикул", "Товар", "Кол-во", "Факт", "Статус", "Ячейки"),
                    products = products
                ))

                // Записываем данные в лист
                dataRows.forEachIndexed { rowIndex, rowData ->
                    val row = sheet.createRow(rowIndex)
                    rowData.forEachIndexed { colIndex, cellValue ->
                        val cell = row.createCell(colIndex)
                        cell.setCellValue(cellValue)
                    }
                }

                // Сохраняем workbook в output stream
                workbook.write(outputStream)
                workbook.close()
            }

            return fileName // Возвращаем имя файла вместо полного пути

        } catch (e: Exception) {
            // В случае ошибки пытаемся удалить созданный файл из MediaStore
            try {
                resolver.delete(uri, null, null)
            } catch (deleteException: Exception) {
                android.util.Log.e("EXPORT", "Ошибка удаления файла из MediaStore: ${deleteException.message}")
            }
            throw e
        }
    }

    /**
     * Очистить все данные и БД
     */
    fun clearAllData() {
        viewModelScope.launch {
            // Очищаем БД через Repository
            productRepository.clearAllProducts()
        }
        
        _products.value = emptyList()
        _filteredProducts.value = emptyList()
        _searchQuery.value = ""
        _fileLoadState.value = FileLoadState.Idle
        _exportState.value = ExportState.Idle
        _debugLogs.value = emptyList()
    }

    /**
     * Получить все debug логи как строку
     */
    fun getDebugLogsAsString(): String {
        return _debugLogs.value.joinToString("\n")
    }

    /**
     * Сбросить состояние экспорта
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    /**
     * Применить фильтр поиска к текущему списку товаров
     */
     private fun applySearchFilter() {
         val query = _searchQuery.value.trim()
         val allProducts = _products.value
         val logMessage = "applySearchFilter: query='$query', allProducts.size=${allProducts.size}"
         android.util.Log.d("DEBUG", logMessage)
         addDebugLog(logMessage)

         if (query.isBlank()) {
             _filteredProducts.value = allProducts
             val noQueryLog = "applySearchFilter: no query, filteredProducts.size=${_filteredProducts.value.size}"
             android.util.Log.d("DEBUG", noQueryLog)
             addDebugLog(noQueryLog)
         } else {
             val filtered = allProducts.filter { product ->
                 // Поиск по артикулу
                 product.article.contains(query, ignoreCase = true) ||
                 // Поиск по названию товара
                 product.name.contains(query, ignoreCase = true) ||
                 // Поиск по штрихкоду
                 product.barcode.contains(query, ignoreCase = true) ||
                 // Поиск по ячейкам хранения
                 product.getStorageCellsDisplayString().contains(query, ignoreCase = true) ||
                 // Поиск по единице измерения
                 product.unit.contains(query, ignoreCase = true)
             }
             _filteredProducts.value = filtered
             val filterLog = "applySearchFilter: filtered.size=${filtered.size}, first actualQuantity=${filtered.firstOrNull()?.actualQuantity ?: "none"}"
             android.util.Log.d("DEBUG", filterLog)
             addDebugLog(filterLog)
         }
     }

    /**
     * Загрузить сохранённые данные из Room Database
     */
    private fun loadSavedData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("MainViewModel", "loadSavedData: Starting to load data from database")
                productRepository.getAllProducts().collect { products ->
                    android.util.Log.d("MainViewModel", "loadSavedData: Received ${products.size} products from database")
                    if (products.isNotEmpty()) {
                        _products.value = products
                        _filteredProducts.value = products
                        _fileLoadState.value = FileLoadState.Success(
                            ExcelData(
                                fileName = "Сохранённые данные",
                                sheetName = "Лист1",
                                headers = listOf("Артикул", "Товар", "Кол-во", "Факт", "Статус", "Ячейки"),
                                products = products
                            )
                        )
                        android.util.Log.d("DATABASE", "Загружено ${products.size} товаров из базы данных")
                    } else {
                        android.util.Log.d("MainViewModel", "loadSavedData: No products in database")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "loadSavedData: Error loading data from database", e)
                _fileLoadState.value = FileLoadState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}
