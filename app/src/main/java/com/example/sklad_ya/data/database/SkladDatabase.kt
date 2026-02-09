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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SkladDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    
    companion object {
        private const val DATABASE_NAME = "sklad_database"

        /**
         * Миграция с версии 1 на версию 2
         * Удаление колонки price из таблицы products
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаём новую таблицу без колонки price
                db.execSQL("""
                    CREATE TABLE products_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        productId TEXT NOT NULL,
                        article TEXT NOT NULL,
                        name TEXT NOT NULL,
                        barcode TEXT NOT NULL,
                        requiredQuantity REAL NOT NULL,
                        actualQuantity REAL NOT NULL,
                        status TEXT NOT NULL,
                        storageCellsJson TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        comments TEXT NOT NULL,
                        comment TEXT NOT NULL,
                        fileStockQuantity REAL NOT NULL,
                        rowIndex INTEGER NOT NULL,
                        originalDataJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Копируем данные из старой таблицы в новую (без колонки price)
                db.execSQL("""
                    INSERT INTO products_new (
                        id, productId, article, name, barcode, requiredQuantity, actualQuantity,
                        status, storageCellsJson, unit, comments, comment, fileStockQuantity,
                        rowIndex, originalDataJson, createdAt, updatedAt
                    )
                    SELECT
                        id, productId, article, name, barcode, requiredQuantity, actualQuantity,
                        status, storageCellsJson, unit, comments, comment, fileStockQuantity,
                        rowIndex, originalDataJson, createdAt, updatedAt
                    FROM products
                """.trimIndent())

                // Удаляем старую таблицу
                db.execSQL("DROP TABLE products")

                // Переименовываем новую таблицу
                db.execSQL("ALTER TABLE products_new RENAME TO products")
            }
        }

        /**
         * Миграция с версии 2 на версию 3
         * Изменение колонки letter на letterGroup в таблице storage_cells
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Проверяем, существует ли таблица storage_cells
                val tableName = "storage_cells"
                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")

                if (cursor.count > 0) {
                    cursor.close()

                    // Создаём новую таблицу с колонкой letterGroup вместо letter
                    db.execSQL("""
                        CREATE TABLE storage_cells_new (
                            cellString TEXT PRIMARY KEY NOT NULL,
                            letterGroup TEXT NOT NULL,
                            number1 INTEGER NOT NULL,
                            number2 INTEGER NOT NULL,
                            number3 INTEGER NOT NULL
                        )
                    """.trimIndent())

                    // Копируем данные из старой таблицы в новую (преобразуя letter в letterGroup)
                    db.execSQL("""
                        INSERT INTO storage_cells_new (cellString, letterGroup, number1, number2, number3)
                        SELECT cellString, letter, number1, number2, number3
                        FROM storage_cells
                    """.trimIndent())

                    // Удаляем старую таблицу
                    db.execSQL("DROP TABLE storage_cells")

                    // Переименовываем новую таблицу
                    db.execSQL("ALTER TABLE storage_cells_new RENAME TO storage_cells")
                } else {
                    cursor.close()
                }
            }
        }

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
