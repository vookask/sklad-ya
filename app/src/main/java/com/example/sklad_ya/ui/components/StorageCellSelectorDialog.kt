package com.example.sklad_ya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sklad_ya.data.model.StorageCell

@Composable
fun StorageCellSelectorDialog(
    onDismiss: () -> Unit,
    onCellSelected: (StorageCell) -> Unit,
    onSettingsClick: () -> Unit = {},
    onCellDeleted: (StorageCell) -> Unit = {},
    currentCells: List<StorageCell> = emptyList(),
    availableLetterGroups: List<String> = listOf("A", "B", "C", "D", "F", "G", "I", "J", "K", "S", "Y"),
    number1Range: IntRange = 1..13,
    number2Range: IntRange = 1..5,
    number3Range: IntRange = 1..4
) {
    var selectedLetterGroup by remember { mutableStateOf<String?>(null) }
    var cellToDelete by remember { mutableStateOf<StorageCell?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedNumber1 by remember { mutableStateOf<Int?>(null) }
    var selectedNumber2 by remember { mutableStateOf<Int?>(null) }
    var selectedNumber3 by remember { mutableStateOf<Int?>(null) }

    val numbers1 = number1Range.toList()
    val numbers2 = number2Range.toList()
    val numbers3 = number3Range.toList()

    val isComplete = selectedLetterGroup != null && selectedNumber1 != null &&
                     selectedNumber2 != null && selectedNumber3 != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовок с кнопкой настроек
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Добавить ячейку",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки ячеек",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Выбор буквы
                Text(
                    text = "Буква:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableLetterGroups.forEach { group ->
                        val isSelected = selectedLetterGroup == group
                        // Адаптивный размер для групп разной длины
                        val boxSize = if (group.length <= 2) 48.dp else 64.dp

                        Box(
                            modifier = Modifier
                                .size(width = boxSize, height = 48.dp)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                .clickable { selectedLetterGroup = group },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = group,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center,
                                fontSize = if (group.length == 1) 20.sp else 16.sp
                            )
                        }
                    }
                }

                // Выбор числа 1
                Text(
                    text = "Число 1 ($number1Range):",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    numbers1.forEach { number ->
                        val isSelected = selectedNumber1 == number
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                .clickable { selectedNumber1 = number },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Выбор числа 2
                Text(
                    text = "Число 2 ($number2Range):",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    numbers2.forEach { number ->
                        val isSelected = selectedNumber2 == number
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                .clickable { selectedNumber2 = number },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Выбор числа 3
                Text(
                    text = "Число 3 ($number3Range):",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    numbers3.forEach { number ->
                        val isSelected = selectedNumber3 == number
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                .clickable { selectedNumber3 = number },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Разделитель
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Текущие ячейки
                Text(
                    text = "Текущие ячейки:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (currentCells.isEmpty()) {
                    // Нет ячеек
                    Text(
                        text = "Нет ячеек",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Список текущих ячеек
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(currentCells) { cell ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cellToDelete = cell
                                        showDeleteDialog = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.toDisplayString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Кнопки действий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Закрыть")
                    }

                    Button(
                        onClick = {
                            selectedLetterGroup?.let { letterGroup ->
                                selectedNumber1?.let { num1 ->
                                    selectedNumber2?.let { num2 ->
                                        selectedNumber3?.let { num3 ->
                                            val cell = StorageCell(letterGroup, num1, num2, num3)
                                            onCellSelected(cell)
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = isComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }

        // Диалог подтверждения удаления ячейки
        if (showDeleteDialog && cellToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    cellToDelete = null
                },
                title = {
                    Text("Удалить ячейку?")
                },
                text = {
                    Text("Ячейка ${cellToDelete!!.toDisplayString()} будет удалена из списка.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onCellDeleted(cellToDelete!!)
                            showDeleteDialog = false
                            cellToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Удалить", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            cellToDelete = null
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}