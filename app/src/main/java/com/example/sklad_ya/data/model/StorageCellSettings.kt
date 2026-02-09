package com.example.sklad_ya.data.model

/**
 * Настройки для генерации ячеек хранения
 */
data class StorageCellSettings(
    val availableLetterGroups: List<String> = DEFAULT_LETTER_GROUPS,
    val number1Range: IntRange = 1..13,
    val number2Range: IntRange = 1..5,
    val number3Range: IntRange = 1..4
) {
    companion object {
        val DEFAULT_LETTER_GROUPS = listOf("A", "B", "C", "D", "F", "G", "I", "J", "K", "S", "Y")

        /**
         * Для обратной совместимости: преобразование из старого формата
         */
        fun fromLetters(letters: List<Char>): StorageCellSettings {
            return StorageCellSettings(
                availableLetterGroups = letters.map { it.toString() }
            )
        }
    }

    /**
     * Получить все отдельные символы из групп (для обратной совместимости)
     */
    fun getAllLetters(): List<Char> {
        return availableLetterGroups.flatMap { it.toList() }
    }
}
