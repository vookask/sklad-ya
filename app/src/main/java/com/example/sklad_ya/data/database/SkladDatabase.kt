package com.example.sklad_ya.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Локальная база данных для приложения Sklad-ya
 */
@Database(
    entities = [ProductEntity::class, StorageCellEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SkladDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    
    companion object {
        private const val DATABASE_NAME = "sklad_database"
        
        @Volatile
        private var INSTANCE: SkladDatabase? = null
        
        /**
         * Получить экземпляр базы данных (Singleton)
         */
        fun getDatabase(context: Context): SkladDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkladDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Уничтожить экземпляр базы данных (для тестов)
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
