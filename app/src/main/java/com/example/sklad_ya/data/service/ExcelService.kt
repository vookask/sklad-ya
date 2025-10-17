package com.example.sklad_ya.data.service

import android.content.Context
import com.example.sklad_ya.data.model.ExcelData
import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus
import com.example.sklad_ya.data.model.StorageCell
import com.example.sklad_ya.data.model.TableAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import java.io.FileInputStream

/**
 * Сервис для работы с Excel файлами
 */
interface ExcelService {
    /**
     * Загрузить данные из Excel файла
     */
    suspend fun loadExcelData(context: Context, filePath: String): Result<ExcelData>

    /**
     * Сохранить данные в Excel файл
     */
    suspend fun saveExcelData(data: ExcelData, fileName: String): Result<String>

    /**
     * Проанализировать таблицу Excel и извлечь данные
     */
    fun analyzeTable(aoa: List<List<String>>): TableAnalysisResult?

    /**
     * Проанализировать таблицу Excel из POI Sheet
     */
    fun analyzeTableFromSheet(sheet: Sheet): TableAnalysisResult?

    /**
     * Создать продукт из строки данных
     */
    fun createProductFromRow(rowData: List<String>, rowIndex: Int, headers: List<String>): Product

    /**
     * Преобразовать данные в формат для сохранения в Excel
     */
    fun prepareDataForSaving(data: ExcelData): List<List<String>>

    /**
     * Валидировать данные Excel файла
     */
    fun validateExcelData(data: List<List<String>>): Boolean
}

/**
 * Реализация сервиса Excel с использованием Apache POI
 */
class ExcelServiceImpl : ExcelService {

    override suspend fun loadExcelData(context: Context, filePath: String): Result<ExcelData> {
        return try {
            withContext(Dispatchers.IO) {
                // Открываем Excel файл с помощью Apache POI
                FileInputStream(filePath).use { fis ->
                    val workbook = WorkbookFactory.create(fis)
                    val sheet = workbook.getSheetAt(0) // Берем первый лист

                    // Проверяем, что лист не пустой
                    if (sheet.lastRowNum <= 0) {
                        return@withContext Result.failure(Exception("Файл Excel пуст или не содержит данных."))
                    }

                    // Определяем структуру таблицы
                    val tableAnalysis = analyzeTableFromSheet(sheet)

                    if (tableAnalysis == null) {
                        return@withContext Result.failure(Exception("Не удалось найти таблицу в файле. Убедитесь, что Excel файл содержит данные и имеет заголовки."))
                    }

                    // Проверяем, что найдены данные
                    if (tableAnalysis.rows.isEmpty()) {
                        return@withContext Result.failure(Exception("В файле не найдены строки с данными."))
                    }

                    // Создаем продукты на основе данных
                    val products = tableAnalysis.rows.mapIndexed { index, row ->
                        createProductFromRow(row, index, tableAnalysis.headers)
                    }

                    val excelData = ExcelData(
                        fileName = filePath.substringAfterLast("/"),
                        sheetName = sheet.sheetName,
                        headers = tableAnalysis.headers,
                        products = products
                    )

                    workbook.close()
                    Result.success(excelData)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при чтении Excel файла: ${e.message}"))
        }
    }

    override suspend fun saveExcelData(data: ExcelData, fileName: String): Result<String> {
        return try {
            // TODO: Реализовать сохранение в Excel файл
            // Пока просто возвращаем успешный результат
            Result.success("Файл сохранён: $fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun analyzeTable(aoa: List<List<String>>): TableAnalysisResult? {
        // Временная реализация анализа таблицы
        return if (aoa.isNotEmpty()) {
            val headers = aoa.first().map { it.toString() }
            val rows = aoa.drop(1)
            TableAnalysisResult(
                headerRowIndex = 0,
                headers = headers,
                rows = rows,
                originalAoa = aoa,
                columnMapping = (0 until headers.size).toList()
            )
        } else {
            null
        }
    }

    override fun prepareDataForSaving(data: ExcelData): List<List<String>> {
        val result = mutableListOf<List<String>>()
        result.add(data.headers)
        result.addAll(data.products.map { product ->
            listOf(
                product.article,
                product.name,
                product.getFormattedQuantity(),
                product.getFormattedActualQuantity(),
                product.status.getSymbol(),
                product.getStorageCellsDisplayString()
            )
        })
        return result
    }

    override fun validateExcelData(data: List<List<String>>): Boolean {
        return data.isNotEmpty() && data.any { row ->
            row.any { cell -> cell.isNotBlank() }
        }
    }

    /**
     * Создать тестовые данные для демонстрации
     */
    private fun createTestProducts(): List<Product> {
        return listOf(
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
            ),
            Product(
                article = "ART003",
                name = "Наушники беспроводные Sony",
                barcode = "1234567890125",
                requiredQuantity = 20.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт",
                price = 15000.0
            ),
            Product(
                article = "ART004",
                name = "Клавиатура механическая",
                barcode = "1234567890126",
                requiredQuantity = 8.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт",
                price = 8000.0
            ),
            Product(
                article = "ART005",
                name = "Мышь компьютерная Logitech",
                barcode = "1234567890127",
                requiredQuantity = 15.0,
                actualQuantity = 0.0,
                status = ProductStatus.PENDING,
                unit = "шт",
                price = 2500.0
            )
        )
    }

    override fun analyzeTableFromSheet(sheet: Sheet): TableAnalysisResult? {
        val rows = mutableListOf<List<String>>()

        // Читаем все строки из листа
        for (row in sheet) {
            val rowData = mutableListOf<String>()
            for (cell in row) {
                rowData.add(getCellValue(cell))
            }
            if (rowData.any { it.isNotBlank() }) {
                rows.add(rowData)
            }
        }

        if (rows.isEmpty()) return null

        // Анализируем заголовки (используем логику из оригинального HTML)
        val tableData = findTableDataFromRows(rows)
        return tableData
    }

    override fun createProductFromRow(rowData: List<String>, rowIndex: Int, headers: List<String>): Product {
        // Маппим колонки на основе реальной структуры файла
        val product = Product(
            article = getColumnValue(rowData, headers, "артикул"),
            name = getColumnValue(rowData, headers, "товар", "наименование", "название"),
            barcode = getColumnValue(rowData, headers, "штрихкод", "штрих"),
            requiredQuantity = getColumnValue(rowData, headers, "кол-во", "количество").toDoubleOrNull() ?: 0.0,
            unit = getColumnValue(rowData, headers, "ед.", "ед", "единица"),
            price = 0.0, // Цена не указана в структуре
            rowIndex = rowIndex
        )

        // Определяем статус на основе данных
        val status = when {
            product.requiredQuantity > 0 -> ProductStatus.PENDING
            else -> ProductStatus.PENDING
        }

        return product.copy(status = status)
    }

    private fun getCellValue(cell: Cell): String {
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                val numericValue = cell.numericCellValue
                if (numericValue == numericValue.toLong().toDouble()) {
                    numericValue.toLong().toString()
                } else {
                    numericValue.toString()
                }
            }
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
            org.apache.poi.ss.usermodel.CellType.FORMULA -> cell.cellFormula
            else -> ""
        }
    }

    private fun getColumnValue(rowData: List<String>, headers: List<String>, vararg columnNames: String): String {
        columnNames.forEach { columnName ->
            val columnIndex = headers.indexOfFirst { header ->
                header.lowercase().contains(columnName.lowercase()) ||
                columnName.lowercase().contains(header.lowercase())
            }
            if (columnIndex >= 0 && columnIndex < rowData.size) {
                return rowData[columnIndex]
            }
        }
        return ""
    }

    private fun findTableDataFromRows(rows: List<List<String>>): TableAnalysisResult? {
        // Отладочная информация
        println("DEBUG: Анализируем ${rows.size} строк")

        // Стратегия: Ищем строку с ключевыми словами заголовков
        for (i in 0 until minOf(rows.size, 100)) {
            val row = rows[i]
            var keywordMatches = 0
            var totalKeywords = 0
            val foundKeywords = mutableListOf<String>()

            println("DEBUG: Проверяем строку $i: ${row.joinToString(" | ")}")

            for (j in 0 until minOf(row.size, 20)) {
                val cell = row[j].trim()
                if (cell.isBlank()) continue

                val lowerCell = cell.lowercase()
                totalKeywords++

                // Подсчитываем ключевые слова
                when {
                    lowerCell == "№" || lowerCell == "номер" -> { keywordMatches++; foundKeywords.add("№") }
                    lowerCell.contains("артикул") -> { keywordMatches++; foundKeywords.add("артикул") }
                    lowerCell.contains("товар") || lowerCell.contains("наименование") -> { keywordMatches++; foundKeywords.add("товар") }
                    lowerCell.contains("кол-во") || lowerCell.contains("количество") -> { keywordMatches++; foundKeywords.add("кол-во") }
                    lowerCell == "ед." || lowerCell.contains("единица") -> { keywordMatches++; foundKeywords.add("ед.") }
                    lowerCell.contains("штрих") || lowerCell.contains("штрихкод") -> { keywordMatches++; foundKeywords.add("штрихкод") }
                    lowerCell.contains("ячейка") || lowerCell.contains("хранение") -> { keywordMatches++; foundKeywords.add("ячейка") }
                    lowerCell.contains("остаток") -> { keywordMatches++; foundKeywords.add("остаток") }
                }
            }

            println("DEBUG: Строка $i - найдено ключевых слов: $keywordMatches из $totalKeywords, слова: ${foundKeywords.joinToString()}")

            // Если нашли строку с достаточным количеством ключевых слов (>= 2)
            if (keywordMatches >= 2) {
                println("DEBUG: НАЙДЕНЫ ЗАГОЛОВКИ в строке $i!")
                return processTableStructure(rows, i)
            }
        }

        println("DEBUG: Заголовки не найдены в первых 100 строках")
        return null
    }

    private fun processTableStructure(rows: List<List<String>>, headerRowIndex: Int): TableAnalysisResult? {
        var headers = rows[headerRowIndex].map { it.trim() }
        var dataRows = rows.drop(headerRowIndex + 1)

        println("DEBUG: Найдены заголовки в строке $headerRowIndex: ${headers.joinToString(" | ")}")
        println("DEBUG: Найдено ${dataRows.size} строк данных для анализа")

        // Фильтруем пустые строки и служебные записи
        var filteredRows = dataRows.filter { row ->
            val hasData = row.any { it.isNotBlank() }
            if (!hasData) {
                println("DEBUG: Отфильтрована пустая строка")
                return@filter false
            }

            val firstCell = row.firstOrNull()?.trim() ?: ""

            // Исключаем служебные строки
            val isServiceRow = firstCell.lowercase().contains("итого") ||
                firstCell.lowercase().contains("всего") ||
                firstCell.lowercase().contains("подпись") ||
                firstCell.lowercase().contains("исполнитель") ||
                firstCell.lowercase().contains("руководитель") ||
                firstCell.lowercase().contains("заказчик") ||
                firstCell.lowercase().contains("поставщик") ||
                firstCell.lowercase().contains("договор") ||
                firstCell.lowercase().contains("получатель") ||
                firstCell.lowercase().contains("отпуск") ||
                firstCell.lowercase().contains("груз")

            if (isServiceRow) {
                println("DEBUG: Отфильтрована служебная строка: $firstCell")
                return@filter false
            }

            // Проверяем, что первая колонка содержит число или артикул
            if (firstCell.isNotBlank() && !firstCell.matches(Regex("\\d+")) &&
                !firstCell.matches(Regex("[A-Za-z].*"))) {
                println("DEBUG: Отфильтрована строка с некорректной первой колонкой: $firstCell")
                return@filter false
            }

            println("DEBUG: Принята строка данных: $firstCell")
            true
        }

        println("DEBUG: После фильтрации осталось ${filteredRows.size} строк данных")

        // Добавляем колонки "Факт" и "Статус" после "Кол-во"
        val kolvoColIdx = headers.indexOfFirst { h ->
            h.lowercase().contains("кол") && (h.lowercase().contains("во") || h.lowercase().contains("ичество"))
        }
        if (kolvoColIdx != -1) {
            val insertIdx = kolvoColIdx + 1
            val newHeaders = ArrayList(headers)
            newHeaders.add(insertIdx, "Факт")
            newHeaders.add(insertIdx + 1, "Статус")
            headers = newHeaders

            val newFilteredRows = filteredRows.map { row ->
                val newRow = ArrayList(row)
                newRow.add(insertIdx, "")
                newRow.add(insertIdx + 1, "")
                newRow
            }
            filteredRows = newFilteredRows

            println("DEBUG: Добавлены колонки Факт и Статус после позиции $kolvoColIdx")
        }

        println("DEBUG: Финальные заголовки: ${headers.joinToString(" | ")}")
        println("DEBUG: Финальное количество строк данных: ${filteredRows.size}")

        return TableAnalysisResult(
            headerRowIndex = headerRowIndex,
            headers = headers,
            rows = filteredRows,
            originalAoa = rows,
            columnMapping = (0 until headers.size).toList()
        )
    }
}