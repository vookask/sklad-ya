package com.example.sklad_ya.data.model

/**
 * Ячейка хранения товара
 * Формат: Группа букв + Число1-Число2-Число3
 * Пример: A1-1-1, TZ5-2-3, USD13-3-4
 */
data class StorageCell(
    val letterGroup: String,   // Группа букв: A, TZ, USD и т.д.
    val number1: Int,          // Первое число: 1-13
    val number2: Int,          // Второе число: 1-3
    val number3: Int           // Третье число: 1-4
) {
    /**
     * Получить строковое представление ячейки
     */
    fun toDisplayString(): String {
        return "$letterGroup$number1-$number2-$number3"
    }

    /**
     * Создать ячейку из строки
     */
    companion object {
        fun fromString(cellString: String): StorageCell? {
            return try {
                // Regex поддерживает 1-3 буквы в начале
                val regex = Regex("([A-Z]{1,3})(\\d+)-(\\d+)-(\\d+)")
                val match = regex.find(cellString.trim())
                if (match != null) {
                    val (letterGroup, num1, num2, num3) = match.destructured
                    StorageCell(
                        letterGroup = letterGroup,
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
        return letterGroup.isNotBlank() &&
                letterGroup.all { it.isLetter() } &&
                letterGroup.length <= 3 &&
                number1 in 1..99 &&
                number2 in 1..99 &&
                number3 in 1..99
    }
}

/**
 * Список всех доступных букв для ячеек хранения (устарело, используйте StorageCellSettings)
 */
@Deprecated("Use StorageCellSettings.availableLetterGroups instead")
val AVAILABLE_CELL_LETTERS = listOf('A', 'B', 'C', 'D', 'F', 'G', 'I', 'J', 'K', 'S', 'Y')