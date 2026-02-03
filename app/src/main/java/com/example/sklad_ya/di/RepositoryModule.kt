package com.example.sklad_ya.di

import android.content.Context
import com.example.sklad_ya.data.repository.ProductRepository
import com.example.sklad_ya.data.repository.ProductRepositoryImpl
import com.example.sklad_ya.data.database.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI модуль для репозитория товаров
 * Предоставляет реализацию ProductRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Предоставляет реализацию ProductRepository
     */
    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao,
        @ApplicationContext context: Context
    ): ProductRepository {
        return ProductRepositoryImpl(productDao, context)
    }
}
