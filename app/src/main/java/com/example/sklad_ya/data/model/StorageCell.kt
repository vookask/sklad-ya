package com.example.sklad_ya.data.model

/**
 * Ячейка хранения товара
 * Формат: Буква + Число1-Число2-Число3
 * Пример: A1-1-1, B5-2-3, S13-3-4
 */
data class StorageCell(
    val letter: Char,        // Буква: A, B, C, D, F, I, J, K, S
    val number1: Int,       // Первое число: 1-13
    val number2: Int,       // Второе число: 1-3
    val number3: Int        // Третье число: 1-4
) {
    /**
     * Получить строковое представление ячейки
     */
    fun toDisplayString(): String {
        return "$letter$number1-$number2-$number3"
    }

    /**
     * Создать ячейку из строки
     */
    companion object {
        fun fromString(cellString: String): StorageCell? {
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
    }

    /**
     * Проверить корректность ячейки
     */
    fun isValid(): Boolean {
        return letter in listOf('A', 'B', 'C', 'D', 'F', 'I', 'J', 'K', 'S') &&
                number1 in 1..13 &&
                number2 in 1..3 &&
                number3 in 1..4
    }
}

/**
 * Список всех доступных букв для ячеек хранения
 */
val AVAILABLE_CELL_LETTERS = listOf('A', 'B', 'C', 'D', 'F', 'I', 'J', 'K', 'S')