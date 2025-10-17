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
     * Очистить все данные
     */
    fun clearAllData() {
        _products.value = emptyList()
        _filteredProducts.value = emptyList()
        _searchQuery.value = ""
        _fileLoadState.value = FileLoadState.Idle
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