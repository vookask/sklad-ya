package com.example.sklad_ya.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sklad_ya.data.model.ExcelData
import com.example.sklad_ya.data.model.FileLoadState
import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus
import com.example.sklad_ya.data.service.ExcelService
import com.example.sklad_ya.data.service.ExcelServiceImpl
import com.example.sklad_ya.data.service.FileService
import com.example.sklad_ya.data.service.FileServiceImpl
import com.example.sklad_ya.data.service.SearchService
import com.example.sklad_ya.data.service.SearchServiceImpl
import com.example.sklad_ya.data.service.StorageCellService
import com.example.sklad_ya.data.service.StorageCellServiceImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
class MainViewModel : ViewModel() {

    // Сервисы
    private val excelService: ExcelService = ExcelServiceImpl()
    private val fileService: FileService = FileServiceImpl()
    private val searchService: SearchService = SearchServiceImpl()
    private val storageCellService: StorageCellService = StorageCellServiceImpl()

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
        // При инициализации загружаем сохранённые данные
        loadSavedData()
        // Загружаем тестовые данные для демонстрации
        loadTestData()
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
                                _filteredProducts.value = excelData.products
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
                unit = "шт",
                price = 50000.0
            ),
            Product(
                article = "ART002",
                name = "Ноутбук ASUS VivoBook",
                barcode = "1234567890124",
                requiredQuantity = 5.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт",
                price = 75000.0
            )
        )
        _filteredProducts.value = _products.value
    }

    /**
     * Обновить фактическое количество товара
     */
    fun updateProductQuantity(productId: String, quantity: Double) {
        val currentProducts = _products.value
        val updatedProducts = currentProducts.map { product ->
            if (product.id == productId) {
                product.updateActualQuantity(quantity)
            } else {
                product
            }
        }
        _products.value = updatedProducts
        applySearchFilter()
    }

    /**
     * Добавить ячейку хранения для товара
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
        applySearchFilter() // Обновляем отфильтрованный список после добавления ячейки
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
        _filteredProducts.value = _products.value
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
     * Очистить все данные
     */
    fun clearAllData() {
        _products.value = emptyList()
        _filteredProducts.value = emptyList()
        _searchQuery.value = ""
        _fileLoadState.value = FileLoadState.Idle
        _exportState.value = ExportState.Idle
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
        if (query.isBlank()) {
            _filteredProducts.value = _products.value
        } else {
            _filteredProducts.value = _products.value.filter { product ->
                // Поиск по артикулу
                product.article.contains(query, ignoreCase = true) ||
                // Поиск по названию товара
                product.name.contains(query, ignoreCase = true) ||
                // Поиск по штрихкоду
                product.barcode.contains(query, ignoreCase = true) ||
                // Поиск по ячейкам хранения
                product.getStorageCellsDisplayString().contains(query, ignoreCase = true) ||
                // Поиск по единице измерения
                product.unit.contains(query, ignoreCase = true) ||
                // Поиск по цене (если цена содержит запрос как строку)
                product.price.toString().contains(query)
            }
        }
    }

    /**
     * Загрузить сохранённые данные (заглушка для будущего функционала)
     */
    private fun loadSavedData() {
        // TODO: Реализовать загрузку из локальной базы данных или SharedPreferences
        _filteredProducts.value = emptyList()
    }
}