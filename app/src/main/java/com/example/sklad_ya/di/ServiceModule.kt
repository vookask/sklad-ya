package com.example.sklad_ya.di

import com.example.sklad_ya.data.service.ExcelService
import com.example.sklad_ya.data.service.ExcelServiceImpl
import com.example.sklad_ya.data.service.FileService
import com.example.sklad_ya.data.service.FileServiceImpl
import com.example.sklad_ya.data.service.SearchService
import com.example.sklad_ya.data.service.SearchServiceImpl
import com.example.sklad_ya.data.service.StorageCellService
import com.example.sklad_ya.data.service.StorageCellServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI модуль для сервисов
 * Предоставляет реализации сервисов для внедрения зависимостей
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    /**
     * Предоставляет сервис для работы с Excel
     */
    @Provides
    @Singleton
    fun provideExcelService(): ExcelService {
        return ExcelServiceImpl()
    }

    /**
     * Предоставляет сервис для работы с файлами
     */
    @Provides
    @Singleton
    fun provideFileService(): FileService {
        return FileServiceImpl()
    }

    /**
     * Предоставляет сервис для поиска товаров
     */
    @Provides
    @Singleton
    fun provideSearchService(): SearchService {
        return SearchServiceImpl()
    }

    /**
     * Предоставляет сервис для работы с ячейками хранения
     */
    @Provides
    @Singleton
    fun provideStorageCellService(): StorageCellService {
        return StorageCellServiceImpl()
    }
}
