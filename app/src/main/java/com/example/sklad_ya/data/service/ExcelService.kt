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
        val startTime = System.currentTimeMillis()
        android.util.Log.d("EXCEL_PERFORMANCE", "Начало загрузки Excel файла: $filePath")

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
                    val endTime = System.currentTimeMillis()
                    val loadTime = endTime - startTime
                    android.util.Log.d("EXCEL_PERFORMANCE", "Excel файл загружен за $loadTime мс, найдено продуктов: ${excelData.products.size}")
                    Result.success(excelData)
                }
            }
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            val loadTime = endTime - startTime
            android.util.Log.e("EXCEL_PERFORMANCE", "Ошибка загрузки Excel файла за $loadTime мс: ${e.message}")
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
        val article = getColumnValue(rowData, headers, "артикул")
        val name = getColumnValue(rowData, headers, "товар", "наименование", "название", "работы", "услуги", "наименование товара")
        val barcode = getColumnValue(rowData, headers, "штрихкод", "штрих")
        val requiredQuantity = getColumnValue(rowData, headers, "кол-во", "количество", "колво").toDoubleOrNull() ?: 0.0
        // Факт всегда пустой при загрузке - заполняется в приложении
        val actualQuantity = 0.0
        val unit = getColumnValue(rowData, headers, "ед.", "ед", "единица", "ед.изм")
        val storageCellsStr = getColumnValue(rowData, headers, "ячейка", "хранение", "ячейки", "место хранения")

        // Парсим ячейки хранения (могут быть через запятую)
        val storageCells = if (storageCellsStr.isNotBlank()) {
            storageCellsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                .mapNotNull { StorageCell.fromString(it) }
        } else {
            emptyList()
        }

        val product = Product(
            article = article,
            name = name,
            barcode = barcode,
            requiredQuantity = requiredQuantity,
            actualQuantity = actualQuantity, // Используем данные из колонки "Остаток"
            unit = unit,
            price = 0.0, // Цена не указана в структуре
            rowIndex = rowIndex,
            storageCells = storageCells
        )

        // Определяем статус на основе данных
        val status = when {
            product.requiredQuantity > 0 && product.actualQuantity > 0 -> {
                if (product.requiredQuantity == product.actualQuantity) {
                    ProductStatus.MATCH // ✓ Совпадает
                } else {
                    ProductStatus.MISMATCH // ⚠ Не совпадает
                }
            }
            product.requiredQuantity > 0 -> ProductStatus.PENDING // Ожидает
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
        // Для более точного поиска используем exact match сначала
        columnNames.forEach { columnName ->
            val exactMatchIndex = headers.indexOfFirst { header ->
                header.trim().lowercase() == columnName.lowercase()
            }
            if (exactMatchIndex >= 0 && exactMatchIndex < rowData.size) {
                return rowData[exactMatchIndex]
            }

            // Если точное совпадение не найдено, ищем по частичному совпадению
            val partialMatchIndex = headers.indexOfFirst { header ->
                header.lowercase().contains(columnName.lowercase()) ||
                columnName.lowercase().contains(header.lowercase())
            }
            if (partialMatchIndex >= 0 && partialMatchIndex < rowData.size) {
                return rowData[partialMatchIndex]
            }
        }
        return ""
    }

    private fun findTableDataFromRows(rows: List<List<String>>): TableAnalysisResult? {
        val startTime = System.currentTimeMillis()
        android.util.Log.d("EXCEL_PERFORMANCE", "Начало анализа ${rows.size} строк данных")

        // Оптимизированная стратегия: Ищем табличные данные более эффективно
        val searchStartIndex = maxOf(0, rows.size - 100) // Начинаем поиск с последних 100 строк

        // Предварительная фильтрация строк для ускорения поиска
        val candidateRows = rows.mapIndexed { index, row ->
            val firstCell = row.firstOrNull()?.trim() ?: ""
            val isService = isServiceRow(firstCell) || firstCell.lowercase().contains("итого") || firstCell.lowercase().contains("всего")
            val hasContent = row.size >= 2 && row.any { it.isNotBlank() }
            if (isService || !hasContent) null else Pair(index, row)
        }.filterNotNull()

        for ((i, row) in candidateRows.filter { it.first >= searchStartIndex }) {
            var keywordMatches = 0.0

            for (j in 0 until minOf(row.size, 20)) {
                val cell = row[j].trim()
                if (cell.isBlank()) continue

                val lowerCell = cell.lowercase()

                // Подсчитываем ключевые слова для табличных заголовков
                when {
                    lowerCell == "№" || lowerCell == "номер" -> keywordMatches += 2.0
                    lowerCell.contains("артикул") -> keywordMatches += 2.0
                    lowerCell.contains("товар") || lowerCell.contains("наименование") || lowerCell.contains("работы") || lowerCell.contains("услуги") -> keywordMatches += 2.0
                    lowerCell.contains("кол-во") || lowerCell.contains("количество") || lowerCell.contains("колво") -> keywordMatches += 2.0
                    lowerCell == "ед." || lowerCell.contains("единица") || lowerCell.contains("ед") -> keywordMatches += 1.5
                    lowerCell.contains("штрих") || lowerCell.contains("штрихкод") -> keywordMatches += 1.5
                    lowerCell.contains("ячейка") || lowerCell.contains("хранение") -> keywordMatches += 1.5
                    lowerCell.contains("остаток") -> keywordMatches += 1.5
                }
            }

            // Если нашли строку с высоким рейтингом ключевых слов (>= 3.0)
            if (keywordMatches >= 3.0) {
                return processTableStructure(rows, i)
            }
        }

        // Стратегия 2: Если не нашли в конце, ищем по всему файлу с более мягкими критериями
        for ((i, row) in candidateRows.filter { it.first < searchStartIndex }) {

            var keywordMatches = 0.0

            for (j in 0 until minOf(row.size, 20)) {
                val cell = row[j].trim()
                if (cell.isBlank()) continue

                val lowerCell = cell.lowercase()

                // Ищем ключевые слова табличных заголовков
                when {
                    lowerCell == "№" -> keywordMatches += 2.0
                    lowerCell.contains("артикул") -> keywordMatches += 2.0
                    lowerCell.contains("товар") || lowerCell.contains("наименование") -> keywordMatches += 2.0
                    lowerCell.contains("кол-во") || lowerCell.contains("количество") -> keywordMatches += 2.0
                }
            }

            if (keywordMatches >= 2.5) {
                return processTableStructure(rows, i)
            }
        }

        // Стратегия 3: Если не нашли заголовки, ищем табличную структуру по другим признакам
        // Ищем строки где первая колонка содержит числа, а остальные колонки заполнены
        for ((i, row) in candidateRows) {
            val firstCell = row.firstOrNull()?.trim() ?: ""

            // Проверяем, является ли первая колонка числом
            val isNumber = firstCell.matches(Regex("\\d+"))

            // Проверяем, есть ли в строке ключевые слова или артикулы
            val hasProductKeywords = row.any { cell ->
                val lower = cell.lowercase()
                lower.contains("valmo") || lower.matches(Regex("арт\\d+"))
            }

            // Проверяем, что строка имеет достаточную длину (минимум 3 колонки)
            if (isNumber && row.size >= 3 && hasProductKeywords) {
                // Ищем заголовки выше этой строки только среди кандидатов
                for ((j, headerRow) in candidateRows.filter { it.first in (i - 20).coerceAtLeast(0) until i }) {
                    val headerMatches = countHeaderKeywords(headerRow)
                    if (headerMatches >= 1.5) {
                        return processTableStructure(rows, j)
                    }
                }
            }
        }

        val endTime = System.currentTimeMillis()
        val analysisTime = endTime - startTime
        android.util.Log.d("EXCEL_PERFORMANCE", "Анализ данных завершен за $analysisTime мс")
        return null
    }

    private fun isServiceRow(firstCell: String): Boolean {
        val lower = firstCell.lowercase()
        return lower.contains("поступление товаров") ||
               lower.contains("исполнитель") ||
               lower.contains("заказчик") ||
               lower.contains("поставщик") ||
               lower.contains("получатель") ||
               lower.contains("отпуск") ||
               lower.contains("груз") ||
               lower.contains("договор") ||
               lower.contains("руководитель") ||
               lower.contains("подпись") ||
               lower.contains("ооо") ||
               lower.contains("инн") ||
               lower.contains("кпп") ||
               lower.contains("телефон") ||
               lower.contains("адрес") ||
               lower.contains("москва") ||
               lower.contains("суздальская") ||
               lower.contains("шакман") ||
               lower.contains("рустрак") ||
               firstCell.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || // даты формата YYYY-MM-DD
               firstCell.matches(Regex("\\d{1,2}\\s+[а-яё]+\\s+\\d{4}")) // даты формата "26 сентября 2025"
    }

    private fun countHeaderKeywords(row: List<String>): Double {
        var matches = 0.0
        for (cell in row) {
            val lower = cell.lowercase()
            when {
                lower == "№" || lower == "номер" -> matches += 1.5
                lower.contains("артикул") -> matches += 1.5
                lower.contains("товар") || lower.contains("наименование") -> matches += 1.5
                lower.contains("кол-во") || lower.contains("количество") -> matches += 1.5
                lower == "ед." || lower.contains("единица") -> matches += 1.0
                lower.contains("штрих") || lower.contains("штрихкод") -> matches += 1.0
                lower.contains("ячейка") || lower.contains("хранение") -> matches += 1.0
                lower.contains("остаток") -> matches += 1.0
            }
        }
        return matches
    }

    private fun processTableStructure(rows: List<List<String>>, headerRowIndex: Int): TableAnalysisResult? {
        var headers = rows[headerRowIndex].map { it.trim() }
        var dataRows = rows.drop(headerRowIndex + 1)

        // Фильтруем пустые строки и служебные записи
        var filteredRows = dataRows.filter { row ->
            val hasData = row.any { it.isNotBlank() }
            if (!hasData) {
                return@filter false
            }

            val firstCell = row.firstOrNull()?.trim() ?: ""

            // Исключаем служебные строки с помощью улучшенной функции
            val isServiceRow = isServiceRow(firstCell) ||
                firstCell.lowercase().contains("итого") ||
                firstCell.lowercase().contains("всего")

            if (isServiceRow) {
                return@filter false
            }

            // Проверяем, что первая колонка содержит число или артикул
            if (firstCell.isNotBlank() && !firstCell.matches(Regex("\\d+")) &&
                !firstCell.matches(Regex("[A-Za-z].*")) &&
                !firstCell.matches(Regex("VALMO\\d+"))) { // Добавляем поддержку артикулов VALMO
                return@filter false
            }

            true
        }

        // Добавляем колонки "Факт", "Статус" и "Остаток из файла" после "Кол-во"
        val kolvoColIdx = headers.indexOfFirst { h ->
            h.lowercase().contains("кол") && (h.lowercase().contains("во") || h.lowercase().contains("ичество"))
        }
        if (kolvoColIdx != -1) {
            val insertIdx = kolvoColIdx + 1
            val newHeaders = ArrayList(headers)
            newHeaders.add(insertIdx, "Факт")
            newHeaders.add(insertIdx + 1, "Статус")
            newHeaders.add(insertIdx + 2, "Остаток из файла")
            headers = newHeaders

            val newFilteredRows = filteredRows.mapIndexed { filteredIndex, row ->
                val newRow = ArrayList(row)
                newRow.add(insertIdx, "") // Факт - пустой
                newRow.add(insertIdx + 1, "") // Статус - пустой

                // Остаток из файла добавляем из оригинальной строки данных
                val originalRowIndex = headerRowIndex + 1 + filteredIndex
                if (originalRowIndex < rows.size) {
                    val originalRow = rows[originalRowIndex]
                    val fileRemainder = getColumnValue(originalRow, headers, "остаток")
                    newRow.add(insertIdx + 2, fileRemainder) // Остаток из файла
                } else {
                    newRow.add(insertIdx + 2, "") // Остаток из файла
                }

                newRow
            }
            filteredRows = newFilteredRows
        }

        return TableAnalysisResult(
            headerRowIndex = headerRowIndex,
            headers = headers,
            rows = filteredRows,
            originalAoa = rows,
            columnMapping = (0 until headers.size).toList()
        )
    }
}