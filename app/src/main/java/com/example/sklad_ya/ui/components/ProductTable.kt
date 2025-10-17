package com.example.sklad_ya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    Column(modifier = modifier) {
        // Заголовки таблицы
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            TableHeaderCell("Артикул", Modifier.width(120.dp))
            TableHeaderCell("Товар", Modifier.width(200.dp))
            TableHeaderCell("Кол-во", Modifier.width(80.dp))
            TableHeaderCell("Факт", Modifier.width(80.dp))
            TableHeaderCell("Статус", Modifier.width(80.dp))
            TableHeaderCell("Ячейки", Modifier.width(150.dp))
        }

        // Разделитель заголовка
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        // Строки товаров
        LazyColumn {
            items(products) { product ->
                ProductRow(
                    product = product,
                    onQuantityChange = { quantity ->
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
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline),
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
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TableCell(product.article, Modifier.width(120.dp))
        TableCell(product.name, Modifier.width(200.dp))

        // Требуемое количество
        TableCell(product.getFormattedQuantity(), Modifier.width(80.dp))

        // Редактируемое фактическое количество
        EditableQuantityCell(
            value = product.getFormattedActualQuantity(),
            onValueChange = { newValue ->
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
            modifier = Modifier.width(150.dp)
        )
    }

    // Разделитель строк
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
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
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
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
    var isEditing by remember { mutableStateOf(false) }

    BasicTextField(
        value = textValue,
        onValueChange = { newValue ->
            textValue = newValue
            isEditing = true
            // Автосохранение через небольшую задержку
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // Ждём 1 секунду после последнего изменения
                if (isEditing) {
                    onValueChange(newValue)
                    isEditing = false
                }
            }
        },
        modifier = modifier
            .padding(4.dp)
            .border(
                width = 1.dp,
                color = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            .padding(8.dp),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true
    )
}

@Composable
private fun StatusCell(
    status: ProductStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ProductStatus.PENDING -> Color.Gray.copy(alpha = 0.1f) to Color.Gray
        ProductStatus.MATCH -> Color.Green.copy(alpha = 0.2f) to Color.Green
        ProductStatus.MISMATCH -> Color.Yellow.copy(alpha = 0.2f) to Color(0xFFF59E0B)
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            .clickable { onClick(productId, productCells) }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
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