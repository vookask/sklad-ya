package com.example.sklad_ya.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.sklad_ya.data.model.StorageCellSettings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Менеджер для сохранения и загрузки настроек ячеек
 */
class CellSettingsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "cell_settings_prefs"
        private const val KEY_SETTINGS = "cell_settings"
    }

    /**
     * Сохранить настройки ячеек
     */
    suspend fun saveSettings(settings: StorageCellSettings) = withContext(Dispatchers.IO) {
        try {
            val jsonSettings = JsonSettings(
                letterGroups = settings.availableLetterGroups,
                number1Start = settings.number1Range.first,
                number1End = settings.number1Range.last,
                number2Start = settings.number2Range.first,
                number2End = settings.number2Range.last,
                number3Start = settings.number3Range.first,
                number3End = settings.number3Range.last
            )
            val jsonString = gson.toJson(jsonSettings)
            prefs.edit().putString(KEY_SETTINGS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загрузить настройки ячеек
     */
    suspend fun loadSettings(): StorageCellSettings? = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_SETTINGS, null)
            if (jsonString != null) {
                val jsonSettings = gson.fromJson(jsonString, JsonSettings::class.java)
                StorageCellSettings(
                    availableLetterGroups = jsonSettings.letterGroups,
                    number1Range = jsonSettings.number1Start..jsonSettings.number1End,
                    number2Range = jsonSettings.number2Start..jsonSettings.number2End,
                    number3Range = jsonSettings.number3Start..jsonSettings.number3End
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Очистить сохранённые настройки
     */
    suspend fun clearSettings() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_SETTINGS).apply()
    }

    /**
     * JSON-представление настроек для сериализации
     */
    private data class JsonSettings(
        val letterGroups: List<String>,
        val number1Start: Int,
        val number1End: Int,
        val number2Start: Int,
        val number2End: Int,
        val number3Start: Int,
        val number3End: Int
    )
}
