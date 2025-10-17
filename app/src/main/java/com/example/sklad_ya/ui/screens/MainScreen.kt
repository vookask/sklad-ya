package com.example.sklad_ya.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sklad_ya.data.model.StorageCell
import com.example.sklad_ya.ui.components.ProductTable
import com.example.sklad_ya.ui.components.SearchBar
import com.example.sklad_ya.ui.components.StatusMessage
import com.example.sklad_ya.ui.components.StorageCellSelectorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    // Состояния из ViewModel
    val fileLoadState by viewModel.fileLoadState.collectAsState()
    val products by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Состояния для диалога выбора ячеек
    var showCellSelectorDialog by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var currentStorageCells by remember { mutableStateOf<List<StorageCell>>(emptyList()) }

    // Состояние для диалога подтверждения очистки
    var showClearConfirmationDialog by remember { mutableStateOf(false) }

    // Launcher для выбора файлов
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // Загружаем файл Excel по URI
            viewModel.loadExcelFile(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Приёмка товаров в ячейки") },
                actions = {
                    // Кнопка загрузки файла
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                        "application/vnd.ms-excel", // .xls
                        "application/octet-stream" // Общий тип для файлов
                    )) }) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Загрузить Excel"
                        )
                    }

                    // Кнопка сохранения
                    IconButton(
                        onClick = { /* TODO: Реализовать сохранение */ },
                        enabled = products.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Сохранить Excel"
                        )
                    }

                    // Кнопка очистки
                    IconButton(
                        onClick = { showClearConfirmationDialog = true },
                        enabled = products.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить таблицу"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Строка поиска
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onClearSearch = { viewModel.clearSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Сообщение о статусе
                StatusMessage(
                    fileLoadState = fileLoadState,
                    productCount = filteredProducts.size,
                    totalCount = products.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // Таблица товаров
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        fileLoadState is com.example.sklad_ya.data.model.FileLoadState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        filteredProducts.isEmpty() && searchQuery.isEmpty() -> {
                            // Пустое состояние
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Загрузите Excel файл для начала работы",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { filePickerLauncher.launch(arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel",
                                        "application/octet-stream"
                                    )) }
                                ) {
                                    Text("Выбрать файл")
                                }
                            }
                        }
                        else -> {
                            ProductTable(
                                products = filteredProducts,
                                onProductQuantityUpdate = { productId, quantity ->
                                    viewModel.updateProductQuantity(productId, quantity)
                                },
                                onStorageCellClick = { productId, currentCells ->
                                    selectedProductId = productId
                                    currentStorageCells = currentCells
                                    showCellSelectorDialog = true
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    )

    // Диалог подтверждения очистки таблицы
    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = { Text("Подтверждение очистки") },
            text = { Text("Вы уверены, что хотите очистить таблицу? Все данные будут потеряны.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmationDialog = false
                    }
                ) {
                    Text("Очистить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmationDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выбора ячеек хранения
    if (showCellSelectorDialog) {
        StorageCellSelectorDialog(
            onDismiss = {
                showCellSelectorDialog = false
                selectedProductId = null
                currentStorageCells = emptyList()
            },
            onCellSelected = { newCell ->
                selectedProductId?.let { productId ->
                    viewModel.addStorageCellToProduct(productId, newCell.toDisplayString())
                }
            },
            currentCells = currentStorageCells
        )
    }
}