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
        val article = getColumnValue(rowData, headers, "артикул")
        val name = getColumnValue(rowData, headers, "товар", "наименование", "название", "работы", "услуги")
        val barcode = getColumnValue(rowData, headers, "штрихкод", "штрих")
        val requiredQuantity = getColumnValue(rowData, headers, "кол-во", "количество", "колво").toDoubleOrNull() ?: 0.0
        val actualQuantity = getColumnValue(rowData, headers, "остаток").toDoubleOrNull() ?: 0.0
        val unit = getColumnValue(rowData, headers, "ед.", "ед", "единица")
        val storageCellsStr = getColumnValue(rowData, headers, "ячейка", "хранение")

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

        // Стратегия 1: Ищем строку с ключевыми словами заголовков
        // Проверяем все строки файла, а не только первые 200
        for (i in 0 until rows.size) {
            val row = rows[i]
            var keywordMatches = 0.0
            var totalKeywords = 0
            val foundKeywords = mutableListOf<String>()

            println("DEBUG: Проверяем строку $i: ${row.joinToString(" | ")}")

            for (j in 0 until minOf(row.size, 20)) {
                val cell = row[j].trim()
                if (cell.isBlank()) continue

                val lowerCell = cell.lowercase()
                totalKeywords++

                // Подсчитываем ключевые слова (расширенный список для сложных файлов)
                when {
                    lowerCell == "№" || lowerCell == "номер" -> { keywordMatches += 1.5; foundKeywords.add("№") }
                    lowerCell.contains("артикул") -> { keywordMatches += 1.5; foundKeywords.add("артикул") }
                    lowerCell.contains("товар") || lowerCell.contains("наименование") || lowerCell.contains("работы") || lowerCell.contains("услуги") -> { keywordMatches += 1.5; foundKeywords.add("товар") }
                    lowerCell.contains("кол-во") || lowerCell.contains("количество") || lowerCell.contains("колво") -> { keywordMatches += 1.5; foundKeywords.add("кол-во") }
                    lowerCell == "ед." || lowerCell.contains("единица") || lowerCell.contains("ед") -> { keywordMatches += 1.0; foundKeywords.add("ед.") }
                    lowerCell.contains("штрих") || lowerCell.contains("штрихкод") -> { keywordMatches += 1.0; foundKeywords.add("штрихкод") }
                    lowerCell.contains("ячейка") || lowerCell.contains("хранение") -> { keywordMatches += 1.0; foundKeywords.add("ячейка") }
                    lowerCell.contains("остаток") -> { keywordMatches += 1.0; foundKeywords.add("остаток") }
                    // Дополнительные ключевые слова для сложных файлов
                    lowerCell.contains("поступление") -> { keywordMatches += 0.3; foundKeywords.add("поступление") }
                    // Ищем числовые паттерны в первых колонках
                    lowerCell.matches(Regex("\\d+")) && j == 0 -> { keywordMatches += 0.8; foundKeywords.add("номер") }
                }
            }

            println("DEBUG: Строка $i - найдено ключевых слов: $keywordMatches из $totalKeywords, слова: ${foundKeywords.joinToString()}")

            // Если нашли строку с достаточным количеством ключевых слов (>= 2.0)
            // Увеличиваем порог для более точного распознавания
            if (keywordMatches >= 2.0) {
                println("DEBUG: НАЙДЕНЫ ЗАГОЛОВКИ в строке $i!")
                return processTableStructure(rows, i)
            }
        }

        // Стратегия 2: Если не нашли заголовки, ищем табличную структуру по другим признакам
        // Ищем строки где первая колонка содержит числа, а остальные колонки заполнены
        for (i in 0 until rows.size) {
            val row = rows[i]

            // Пропускаем явно служебные строки
            if (row.isEmpty() || row.all { it.isBlank() }) continue

            val firstCell = row.firstOrNull()?.trim() ?: ""
            if (firstCell.isBlank()) continue

            // Проверяем, является ли первая колонка числом
            val isNumber = firstCell.matches(Regex("\\d+"))

            // Проверяем, есть ли в строке ключевые слова или артикулы
            val hasProductKeywords = row.any { cell ->
                val lower = cell.lowercase()
                lower.contains("valmo") || lower.matches(Regex("арт\\d+"))
            }

            // Проверяем, что строка имеет достаточную длину (минимум 3 колонки)
            if (isNumber && row.size >= 3 && hasProductKeywords) {
                println("DEBUG: Найдена потенциальная строка данных в позиции $i: $firstCell")
                // Ищем заголовки выше этой строки
                for (j in (i - 20).coerceAtLeast(0) until i) {
                    val headerRow = rows[j]
                    val headerMatches = countHeaderKeywords(headerRow)
                    if (headerMatches >= 1.5) {
                        println("DEBUG: Найдены заголовки выше данных в строке $j!")
                        return processTableStructure(rows, j)
                    }
                }
            }
        }

        println("DEBUG: Заголовки не найдены во всем файле")
        return null
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
                !firstCell.matches(Regex("[A-Za-z].*")) &&
                !firstCell.matches(Regex("VALMO\\d+"))) { // Добавляем поддержку артикулов VALMO
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