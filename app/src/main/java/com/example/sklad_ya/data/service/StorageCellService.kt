package com.example.sklad_ya.data.service

import com.example.sklad_ya.data.model.StorageCell
import com.example.sklad_ya.data.model.AVAILABLE_CELL_LETTERS

/**
 * Сервис для работы с ячейками хранения
 */
interface StorageCellService {
    /**
     * Получить все доступные варианты букв
     */
    fun getAvailableLetters(): List<Char>

    /**
     * Получить доступные номера для указанной буквы
     */
    fun getAvailableNumbers(letter: Char): List<Int>

    /**
     * Проверить корректность ячейки
     */
    fun isValidCell(cell: StorageCell): Boolean

    /**
     * Получить все возможные комбинации ячеек
     */
    fun getAllPossibleCells(): List<StorageCell>

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

    override fun getAvailableLetters(): List<Char> {
        return AVAILABLE_CELL_LETTERS
    }

    override fun getAvailableNumbers(letter: Char): List<Int> {
        return when (letter) {
            in listOf('A', 'B', 'C', 'D', 'F', 'I', 'J', 'K', 'S') -> {
                when (letter) {
                    'A' -> (1..13).toList()
                    'B' -> (1..13).toList()
                    'C' -> (1..13).toList()
                    'D' -> (1..13).toList()
                    'F' -> (1..13).toList()
                    'I' -> (1..13).toList()
                    'J' -> (1..13).toList()
                    'K' -> (1..13).toList()
                    'S' -> (1..13).toList()
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    override fun isValidCell(cell: StorageCell): Boolean {
        return cell.letter in AVAILABLE_CELL_LETTERS &&
                cell.number1 in 1..13 &&
                cell.number2 in 1..3 &&
                cell.number3 in 1..4
    }

    override fun getAllPossibleCells(): List<StorageCell> {
        val cells = mutableListOf<StorageCell>()

        for (letter in AVAILABLE_CELL_LETTERS) {
            val maxNumber1 = 13
            for (number1 in 1..maxNumber1) {
                for (number2 in 1..3) {
                    for (number3 in 1..4) {
                        cells.add(StorageCell(letter, number1, number2, number3))
                    }
                }
            }
        }

        return cells
    }

    override fun parseCell(cellString: String): StorageCell? {
        return try {
            val regex = Regex("([A-Z])(\\d+)-(\\d+)-(\\d+)")
            val match = regex.find(cellString.trim())
            if (match != null) {
                val (letter, num1, num2, num3) = match.destructured
                StorageCell(
                    letter = letter.first(),
                    number1 = num1.toInt(),
                    number2 = num2.toInt(),
                    number3 = num3.toInt()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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