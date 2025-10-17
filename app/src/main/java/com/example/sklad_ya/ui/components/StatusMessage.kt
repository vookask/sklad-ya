package com.example.sklad_ya.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sklad_ya.data.model.FileLoadState

@Composable
fun StatusMessage(
    fileLoadState: FileLoadState,
    productCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая часть - статус загрузки
            Text(
                text = when (fileLoadState) {
                    is FileLoadState.Idle -> "Загрузите Excel для начала работы"
                    is FileLoadState.Loading -> "Загрузка файла..."
                    is FileLoadState.Success -> {
                        val data = fileLoadState.data
                        "Файл: ${data.fileName} | Лист: ${data.sheetName}"
                    }
                    is FileLoadState.Error -> fileLoadState.message
                },
                style = MaterialTheme.typography.bodySmall,
                color = when (fileLoadState) {
                    is FileLoadState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Правая часть - количество товаров
            if (totalCount > 0) {
                Text(
                    text = when {
                        productCount == totalCount -> "Товаров: $totalCount"
                        else -> "Найдено: $productCount из $totalCount"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}