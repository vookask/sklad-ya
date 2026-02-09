package com.example.sklad_ya.data.service

import com.example.sklad_ya.data.model.StorageCell

/**
 * Сервис для работы с ячейками хранения
 */
interface StorageCellService {
    /**
     * Получить все доступные варианты групп букв
     */
    fun getAvailableLetterGroups(): List<String>

    /**
     * Проверить корректность ячейки
     */
    fun isValidCell(cell: StorageCell): Boolean

    /**
     * Получить все возможные комбинации ячеек
     * @param letterGroups Список групп букв
     * @param number1Range Диапазон для первого числа
     * @param number2Range Диапазон для второго числа
     * @param number3Range Диапазон для третьего числа
     */
    fun getAllPossibleCells(
        letterGroups: List<String>,
        number1Range: IntRange = 1..13,
        number2Range: IntRange = 1..5,
        number3Range: IntRange = 1..4
    ): List<StorageCell>

    /**
     * Найти ячейку по строковому представлению
     */
    fun parseCell(cellString: String): StorageCell?

    /**
     * Проверить, свободна ли ячейка
     */
    suspend fun isCellAvailable(cell: StorageCell, excludeProductId: String? = null): Boolean

    /**
     * Занять ячейку для товара
     */
    suspend fun occupyCell(cell: StorageCell, productId: String)

    /**
     * Освободить ячейку
     */
    suspend fun releaseCell(cell: StorageCell)
}

/**
 * Реализация сервиса ячеек хранения
 */
class StorageCellServiceImpl : StorageCellService {
    private val occupiedCells = mutableMapOf<StorageCell, String>() // cell -> productId

    override fun getAvailableLetterGroups(): List<String> {
        // TODO: Загружать из настроек или БД
        return listOf("A", "B", "C", "D", "F", "G", "I", "J", "K", "S", "Y")
    }

    override fun isValidCell(cell: StorageCell): Boolean {
        return cell.isValid()
    }

    override fun getAllPossibleCells(
        letterGroups: List<String>,
        number1Range: IntRange,
        number2Range: IntRange,
        number3Range: IntRange
    ): List<StorageCell> {
        val cells = mutableListOf<StorageCell>()

        for (letterGroup in letterGroups) {
            for (number1 in number1Range) {
                for (number2 in number2Range) {
                    for (number3 in number3Range) {
                        cells.add(StorageCell(letterGroup, number1, number2, number3))
                    }
                }
            }
        }

        return cells
    }

    override fun parseCell(cellString: String): StorageCell? {
        return StorageCell.fromString(cellString)
    }

    override suspend fun isCellAvailable(cell: StorageCell, excludeProductId: String?): Boolean {
        val occupyingProductId = occupiedCells[cell]
        return occupyingProductId == null || occupyingProductId == excludeProductId
    }

    override suspend fun occupyCell(cell: StorageCell, productId: String) {
        occupiedCells[cell] = productId
    }

    override suspend fun releaseCell(cell: StorageCell) {
        occupiedCells.remove(cell)
    }
}