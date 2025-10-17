package com.example.sklad_ya.data.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Сервис для работы с файлами
 */
interface FileService {
    /**
     * Получить локальный путь к файлу по URI
     */
    suspend fun getFilePathFromUri(context: Context, uri: Uri): Result<String>

    /**
     * Получить имя файла по URI
     */
    suspend fun getFileNameFromUri(context: Context, uri: Uri): String?
}

/**
 * Реализация сервиса для работы с файлами
 */
class FileServiceImpl : FileService {

    override suspend fun getFilePathFromUri(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем имя файла
                val fileName = getFileNameFromUri(context, uri)
                    ?: return@withContext Result.failure(Exception("Не удалось получить имя файла. Проверьте разрешения доступа к файлам."))

                // Проверяем, является ли URI локальным файлом
                if (uri.scheme == "file") {
                    return@withContext Result.success(uri.path!!)
                }

                // Для контент URI создаем временный файл
                val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_$fileName")

                // Копируем содержимое из URI во временный файл
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                    }
                } ?: return@withContext Result.failure(Exception("Не удалось открыть поток файла"))

                // Проверяем, что файл был создан и имеет размер > 0
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    return@withContext Result.failure(Exception("Файл не был корректно скопирован"))
                }

                Result.success(tempFile.absolutePath)
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка получения пути к файлу: ${e.message}"))
            }
        }
    }

    override suspend fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}