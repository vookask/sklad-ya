package com.example.sklad_ya.di

import android.content.Context
import com.example.sklad_ya.data.database.ProductDao
import com.example.sklad_ya.data.database.SkladDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI модуль для базы данных
 * Предоставляет экземпляр базы данных и DAO
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Предоставляет экземпляр базы данных
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SkladDatabase {
        return SkladDatabase.getDatabase(context)
    }

    /**
     * Предоставляет DAO для работы с товарами
     */
    @Provides
    @Singleton
    fun provideProductDao(database: SkladDatabase): ProductDao {
        return database.productDao()
    }
}
