package com.example.sklad_ya.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.sklad_ya.data.model.StorageCellSettings

/**
 * Диалог настроек ячеек хранения
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCellSettingsDialog(
    currentSettings: StorageCellSettings,
    onDismiss: () -> Unit,
    onApply: (StorageCellSettings) -> Unit
) {
    // Группы букв (хранятся как строки: "A", "TZ", "USD")
    var letterGroups by remember {
        mutableStateOf(currentSettings.availableLetterGroups)
    }

    // Диапазоны чисел
    var number1Start by remember { mutableStateOf(currentSettings.number1Range.first.toString()) }
    var number1End by remember { mutableStateOf(currentSettings.number1Range.last.toString()) }

    var number2Start by remember { mutableStateOf(currentSettings.number2Range.first.toString()) }
    var number2End by remember { mutableStateOf(currentSettings.number2Range.last.toString()) }

    var number3Start by remember { mutableStateOf(currentSettings.number3Range.first.toString()) }
    var number3End by remember { mutableStateOf(currentSettings.number3Range.last.toString()) }

    var showAddLettersDialog by remember { mutableStateOf(false) }
    var newLettersInput by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 600.dp)
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Настройки ячеек",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Прокручиваемый контент с отступом снизу для клавиатуры
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        end = 0.dp,
                        top = 8.dp,
                        bottom = 200.dp // Дополнительное место снизу для клавиатуры
                    )
                ) {
                    // Секция букв
                    item {
                        Text(
                            text = "Группы букв (A, TZ, USD):",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        // Список групп букв с удалением
                        if (letterGroups.isEmpty()) {
                            Text(
                                text = "Нет букв",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Row с горизонтальной прокруткой для групп букв
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                letterGroups.forEach { group ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = group,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            IconButton(
                                                onClick = {
                                                    letterGroups = letterGroups - group
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Удалить",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Кнопка добавления букв
                        OutlinedButton(
                            onClick = { showAddLettersDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить буквы")
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    // Секция диапазона чисел 1
                    item {
                        NumberRangeInput(
                            label = "Число 1 (до 99)",
                            startValue = number1Start,
                            endValue = number1End,
                            onStartChange = { number1Start = it },
                            onEndChange = { number1End = it }
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    // Секция диапазона чисел 2
                    item {
                        NumberRangeInput(
                            label = "Число 2 (до 99)",
                            startValue = number2Start,
                            endValue = number2End,
                            onStartChange = { number2Start = it },
                            onEndChange = { number2End = it }
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    // Секция диапазона чисел 3
                    item {
                        NumberRangeInput(
                            label = "Число 3 (до 99)",
                            startValue = number3Start,
                            endValue = number3End,
                            onStartChange = { number3Start = it },
                            onEndChange = { number3End = it }
                        )
                    }
                }

                // Кнопки действий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            // Вычисляем диапазоны с проверкой значений
                            val n1Start = number1Start.toIntOrNull()?.coerceIn(1, 99) ?: 1
                            val n1End = number1End.toIntOrNull()?.coerceIn(1, 99) ?: 13
                            val n2Start = number2Start.toIntOrNull()?.coerceIn(1, 99) ?: 1
                            val n2End = number2End.toIntOrNull()?.coerceIn(1, 99) ?: 5
                            val n3Start = number3Start.toIntOrNull()?.coerceIn(1, 99) ?: 1
                            val n3End = number3End.toIntOrNull()?.coerceIn(1, 99) ?: 4

                            // Используем группы букв напрямую
                            val newSettings = StorageCellSettings(
                                availableLetterGroups = letterGroups,
                                number1Range = if (n1Start <= n1End) n1Start..n1End else n1End..n1Start,
                                number2Range = if (n2Start <= n2End) n2Start..n2End else n2End..n2Start,
                                number3Range = if (n3Start <= n3End) n3Start..n3End else n3End..n3Start
                            )
                            onApply(newSettings)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Применить")
                    }
                }
            }
        }
    }

    // Диалог добавления букв
    if (showAddLettersDialog) {
        AlertDialog(
            onDismissRequest = { showAddLettersDialog = false },
            title = { Text("Добавить буквы") },
            text = {
                Column {
                    Text(
                        text = "Введите 1-3 буквы (A, TZ, UDS):",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newLettersInput,
                        onValueChange = { input ->
                            // Автоматически верхний регистр и ограничение 3 символами
                            val filtered = input.uppercase().filter { it.isLetter() }.take(3)
                            newLettersInput = filtered
                        },
                        label = { Text("Буквы") },
                        placeholder = { Text("ABC или TZ или UDS") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (newLettersInput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Проверяем дубликаты групп
                        val hasDuplicates = newLettersInput in letterGroups
                        if (hasDuplicates) {
                            Text(
                                text = "Такая группа уже существует",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Будет добавлено: $newLettersInput",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Добавляем группу букв, проверяя дубликаты
                        if (newLettersInput.isNotEmpty() && newLettersInput !in letterGroups) {
                            letterGroups = letterGroups + newLettersInput
                        }
                        showAddLettersDialog = false
                        newLettersInput = ""
                    }
                ) {
                    Text("Добавить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddLettersDialog = false
                        newLettersInput = ""
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun NumberRangeInput(
    label: String,
    startValue: String,
    endValue: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: с $startValue по $endValue",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Поле "От"
            OutlinedTextField(
                value = startValue,
                onValueChange = { newValue ->
                    // Только цифры, не более 2 символов (до 99)
                    val filtered = newValue.filter { it.isDigit() }.take(2)
                    onStartChange(filtered)
                },
                label = { Text("От") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Text(
                text = "—",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Поле "До"
            OutlinedTextField(
                value = endValue,
                onValueChange = { newValue ->
                    // Только цифры, не более 2 символов (до 99)
                    val filtered = newValue.filter { it.isDigit() }.take(2)
                    onEndChange(filtered)
                },
                label = { Text("До") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}
