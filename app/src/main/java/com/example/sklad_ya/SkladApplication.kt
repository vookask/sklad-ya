package com.example.sklad_ya

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Класс приложения Sklad-ya
 * Инициализирует Hilt для внедрения зависимостей
 */
@HiltAndroidApp
class SkladApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Инициализация БД при старте приложения
        // БД будет автоматически инициализирована Hilt через модули DI
    }
}
