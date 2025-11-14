package com.example.sklad_ya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sklad_ya.data.model.Product
import com.example.sklad_ya.data.model.ProductStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProductTable(
    products: List<Product>,
    onProductQuantityUpdate: (String, Double) -> Unit,
    onStorageCellClick: (String, List<com.example.sklad_ya.data.model.StorageCell>) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(modifier = modifier.clickable(indication = null, interactionSource = null) { focusManager.clearFocus() }) {
        // Заголовки таблицы
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            TableHeaderCell("Артикул", Modifier.width(145.dp))
            TableHeaderCell("Товар", Modifier.width(200.dp))
            TableHeaderCell("Кол-во", Modifier.width(80.dp))
            TableHeaderCell("Факт", Modifier.width(80.dp))
            TableHeaderCell("Статус", Modifier.width(80.dp))
            TableHeaderCell("Ячейки", Modifier.width(150.dp))
            // Добавляем колонку "Остаток" если есть данные об остатках
            if (products.any { it.fileStockQuantity > 0 }) {
                TableHeaderCell("Остаток", Modifier.width(80.dp))
            }
        }

        // Разделитель заголовка
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        // Строки товаров
        LazyColumn {
            items(products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onQuantityChange = { quantity ->
                        android.util.Log.d("DEBUG", "ProductTable: onQuantityChange for product ${product.id}, quantity=$quantity")
                        onProductQuantityUpdate(product.id, quantity)
                    },
                    onStorageCellClick = onStorageCellClick,
                    scrollState = horizontalScrollState
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .padding(12.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ProductRow(
    product: Product,
    onQuantityChange: (Double) -> Unit,
    onStorageCellClick: (String, List<com.example.sklad_ya.data.model.StorageCell>) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    android.util.Log.d("DEBUG", "ProductRow: rendering product ${product.id}, actualQuantity=${product.actualQuantity}")
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TableCell(product.article, Modifier.width(145.dp))
        TableCell(product.name, Modifier.width(200.dp))

        // Требуемое количество
        TableCell(product.getFormattedQuantity(), Modifier.width(80.dp))

        // Редактируемое фактическое количество
        EditableQuantityCell(
            value = product.getFormattedActualQuantity(),
            onValueChange = { newValue ->
                android.util.Log.d("DEBUG", "ProductRow: EditableQuantityCell onValueChange for product ${product.id}, newValue='$newValue'")
                val quantity = newValue.toDoubleOrNull() ?: 0.0
                onQuantityChange(quantity)
            },
            modifier = Modifier.width(80.dp)
        )

        // Статус
        StatusCell(product.status, Modifier.width(80.dp))

        // Ячейки хранения
        StorageCell(
            cells = product.getStorageCellsDisplayString(),
            onClick = onStorageCellClick,
            productId = product.id,
            productCells = product.storageCells,
            modifier = Modifier.width(150.dp),
            multiLine = true
        )

        // Остаток из файла (показываем только если есть данные)
        if (product.fileStockQuantity > 0) {
            TableCell(
                text = if (product.fileStockQuantity % 1.0 == 0.0) {
                    product.fileStockQuantity.toInt().toString()
                } else {
                    product.fileStockQuantity.toString()
                },
                modifier = Modifier.width(80.dp)
            )
        }
    }

    // Разделитель строк
    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .padding(12.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EditableQuantityCell(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember { mutableStateOf(value) }
    android.util.Log.d("DEBUG", "EditableQuantityCell: initial value='$value', textValue='$textValue'")

    BasicTextField(
        value = textValue,
        onValueChange = { newValue ->
            // Фильтруем только цифры и точку
            val filteredValue = newValue.filter { it.isDigit() || it == '.' }
            textValue = filteredValue
        },
        modifier = modifier
            .padding(4.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
            .onFocusChanged { focusState ->
                // Сохраняем значение при потере фокуса
                if (!focusState.isFocused && textValue != value) {
                    android.util.Log.d("DEBUG", "EditableQuantityCell: focus lost, calling onValueChange with '$textValue'")
                    onValueChange(textValue)
                }
            },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
    )
}

@Composable
private fun StatusCell(
    status: ProductStatus,
    modifier: Modifier = Modifier
) {
    val textColor = when (status) {
        ProductStatus.PENDING -> Color.Gray
        ProductStatus.MATCH -> Color.Green
        ProductStatus.MISMATCH -> Color(0xFFF59E0B)
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.getSymbol(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StorageCell(
    cells: String,
    onClick: (String, List<com.example.sklad_ya.data.model.StorageCell>) -> Unit,
    productId: String,
    productCells: List<com.example.sklad_ya.data.model.StorageCell>,
    modifier: Modifier = Modifier,
    multiLine: Boolean = false
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            .clickable { onClick(productId, productCells) }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cells.isNotEmpty()) {
            // Всегда показываем кнопку плюс внизу если есть ячейки
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (multiLine && cells.contains(",")) {
                    // Многострочный режим для нескольких ячеек
                    val cellList = cells.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    cellList.forEach { cell ->
                        Text(
                            text = cell,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                } else {
                    // Обычный режим для одной ячейки
                    Text(
                        text = cells,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Кнопка плюс всегда внизу
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        } else {
            // Обычный режим
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (cells.isNotEmpty()) {
                    Text(
                        text = cells,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        }
    }
}