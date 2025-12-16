# Подробная структура данных проекта Sklad-Ya

## 📁 Общая структура папки `data`

```
app/src/main/java/com/example/sklad_ya/data/
├── model/                    # Модели данных
│   ├── Product.kt           # Модель товара
│   ├── StorageCell.kt       # Модель ячейки хранения  
│   ├── ExcelData.kt         # Данные из Excel
│   └── ProductStatus.kt     # Статусы товаров
├── service/                  # Сервисы бизнес-логики
│   ├── ExcelService.kt      # Работа с Excel файлами
│   ├── FileService.kt       # Работа с файлами
│   ├── SearchService.kt     # Поиск и фильтрация
│   └── StorageCellService.kt # Управление ячейками
└── repository/              # Репозитории данных
    ├── ProductRepository.kt # Репозиторий товаров
    └── FileRepository.kt    # Репозиторий файлов
```

---

## 🏗️ Модели данных (model/)

### 1. `Product.kt` - Модель товара

**Назначение:** Представляет товар в системе приёмки с полной информацией для складского учёта.

**Основные поля:**
- `id: String` - Уникальный идентификатор товара (auto-generated)
- `article: String` - Артикул товара
- `name: String` - Наименование товара
- `barcode: String` - Штрихкод
- `requiredQuantity: Double` - Требуемое количество (из файла)
- `actualQuantity: Double` - Фактическое количество (проверенное)
- `status: ProductStatus` - Статус приёмки (PENDING/MATCH/MISMATCH)
- `storageCells: List<StorageCell>` - Список ячеек хранения
- `unit: String` - Единица измерения
- `price: Double` - Цена товара
- `rowIndex: Int` - Индекс строки в исходном файле
- `originalData: Map<String, String>` - Оригинальные данные из Excel
- `fileStockQuantity: Double` - Остаток из файла Excel
- `comment: String` - Комментарий к товару

**Функции модели:**
- `updateActualQuantity(quantity: Double): Product` - Обновляет фактическое количество и пересчитывает статус
- `addStorageCell(cell: StorageCell): Product` - Добавляет ячейку хранения (без дублирования)
- `removeStorageCell(cellString: String): Product` - Удаляет ячейку хранения
- `getFormattedQuantity(): String` - Форматирует количество для отображения (без .0)
- `getFormattedActualQuantity(): String` - Форматирует фактическое количество (пустая строка для 0)
- `getStorageCellsDisplayString(): String` - Объединяет ячейки через запятую для отображения
- `updateComment(comment: String): Product` - Обновляет комментарий
- `generateId(): String` (companion) - Генерирует уникальные ID

### 2. `StorageCell.kt` - Модель ячейки хранения

**Назначение:** Определяет местоположение товара в складской системе.

**Формат:** Буква + Число1-Число2-Число3 (например: A1-1-1, B5-2-3)

**Поля:**
- `letter: Char` - Буквенный код (A, B, C, D, F, G, I, J, K, S, Y)
- `number1: Int` - Первый номер (1-13)
- `number2: Int` - Второй номер (1-3)  
- `number3: Int` - Третий номер (1-4)

**Функции:**
- `toDisplayString(): String` - Возвращает строковое представление "A1-1-1"
- `fromString(cellString: String): StorageCell?` (companion) - Парсит строку в объект
- `isValid(): Boolean` - Проверяет корректность ячейки
- `AVAILABLE_CELL_LETTERS` - Константа со списком доступных букв

### 3. `ExcelData.kt` - Данные из Excel

**Назначение:** Контейнер для данных, загруженных из Excel файла.

**Поля:**
- `fileName: String` - Имя файла
- `sheetName: String` - Название листа
- `headers: List<String>` - Заголовки колонок
- `products: List<Product>` - Список товаров
- `originalRowIndex: Int` - Индекс строки с заголовками
- `columnMapping: List<Int>` - Маппинг колонок
- `loadTime: Long` - Время загрузки

**Дополнительные классы:**
- `TableAnalysisResult` - Результат анализа таблицы Excel
- `FileLoadState` - Состояния загрузки файла (sealed class)
  - `Idle` - Ожидание
  - `Loading` - Загрузка
  - `Success(data)` - Успех
  - `Error(message)` - Ошибка

### 4. `ProductStatus.kt` - Статусы товаров

**Назначение:** Определяет состояние товара при приёмке.

**Статусы:**
- `PENDING` - Ожидает проверки (серый)
- `MATCH` - Количество совпадает (зелёный, символ ✓)
- `MISMATCH` - Количество не совпадает (жёлтый, символ ⚠)

**Функции:**
- `getSymbol(): String` - Возвращает символ для отображения статуса
- `getColor(): ProductStatusColor` - Возвращает цвет для статуса

**Цвета статусов:**
- `GRAY` - Серый (PENDING)
- `GREEN` - Зелёный (MATCH)  
- `YELLOW` - Жёлтый (MISMATCH)

---

## ⚙️ Сервисы (service/)

### 1. `ExcelService.kt` - Работа с Excel файлами

**Назначение:** Загрузка, анализ и сохранение данных Excel с использованием Apache POI.

**Интерфейс:**
- `loadExcelData(context, filePath): Result<ExcelData>` - Загружает данные из Excel
- `saveExcelData(data, fileName): Result<String>` - Сохраняет данные в Excel
- `analyzeTable(aoa): TableAnalysisResult?` - Анализирует таблицу из массива
- `analyzeTableFromSheet(sheet): TableAnalysisResult?` - Анализирует таблицу из POI Sheet
- `createProductFromRow(rowData, rowIndex, headers): Product` - Создаёт продукт из строки
- `prepareDataForSaving(data): List<List<String>>` - Подготавливает данные для сохранения
- `validateExcelData(data): Boolean` - Валидирует данные Excel

**Реализация `ExcelServiceImpl`:**

**Основные функции:**
- `loadExcelData()` - Полный цикл загрузки с анализом производительности
- `saveExcelData()` - Создание нового Excel файла с данными
- `analyzeTableFromSheet()` - Глубокий анализ структуры таблицы
- `createProductFromRow()` - Интеллектуальное создание продукта с fallback логикой
- `getCellValue(cell): String` - Извлечение значения ячейки с обработкой типов
- `getColumnValue(rowData, headers, *columnNames): String` - Поиск колонки по названию
- `debugExcelColumns()` - Отладочная функция для диагностики колонок

**Алгоритмы анализа:**
- `findTableDataFromRows()` - Многостратегический поиск табличных данных
- `processTableStructure()` - Обработка найденной структуры таблицы
- `isServiceRow()` - Определение служебных строк для исключения
- `countHeaderKeywords()` - Подсчёт ключевых слов в заголовках

### 2. `FileService.kt` - Работа с файлами

**Назначение:** Управление файлами, получение путей и имён файлов.

**Интерфейс:**
- `getFilePathFromUri(context, uri): Result<String>` - Получает локальный путь по URI
- `getFileNameFromUri(context, uri): String?` - Получает имя файла по URI

**Реализация `FileServiceImpl`:**
- Работа с локальными файлами (scheme="file")
- Копирование контент URI во временные файлы
- Проверка целостности скопированных файлов
- Извлечение имён файлов через ContentResolver

### 3. `SearchService.kt` - Поиск и фильтрация

**Назначение:** Обеспечивает поиск, фильтрацию и сортировку товаров.

**Интерфейс:**
- `searchProducts(products, query): List<Product>` - Поиск по артикулу, названию, штрихкоду, ячейкам
- `filterProductsByStatus(products, status): List<Product>` - Фильтрация по статусу
- `filterProductsByStorageCells(products, cellQuery): List<Product>` - Фильтрация по ячейкам
- `sortProducts(products, sortBy): List<Product>` - Сортировка по различным критериям

**Критерии сортировки (`SortCriteria`):**
- `ARTICLE_ASC/DESC` - По артикулу (возрастание/убывание)
- `NAME_ASC/DESC` - По названию (возрастание/убывание)  
- `QUANTITY_ASC/DESC` - По количеству (возрастание/убывание)
- `STATUS` - По статусу

### 4. `StorageCellService.kt` - Управление ячейками

**Назначение:** Управление складскими ячейками, проверка доступности.

**Интерфейс:**
- `getAvailableLetters(): List<Char>` - Все доступные буквы
- `getAvailableNumbers(letter): List<Int>` - Доступные номера для буквы
- `isValidCell(cell): Boolean` - Проверка корректности ячейки
- `getAllPossibleCells(): List<StorageCell>` - Все возможные комбинации
- `parseCell(cellString): StorageCell?` - Парсинг строки в ячейку
- `isCellAvailable(cell, excludeProductId): Boolean` - Проверка доступности
- `occupyCell(cell, productId)` - Занять ячейку
- `releaseCell(cell)` - Освободить ячейку

**Особенности реализации:**
- Отслеживание занятых ячеек через `occupiedCells: MutableMap<StorageCell, String>`
- Поддержка всех 11 буквенных кодов (A, B, C, D, F, G, I, J, K, S, Y)
- Каждая буква имеет 13×3×4 = 156 возможных позиций
- Исключение продукта при проверке занятости для перемещения

---

## 🗄️ Репозитории (repository/)

### 1. `ProductRepository.kt` - Репозиторий товаров

**Назначение:** Абстракция доступа к данным товаров с использованием Flow.

**Интерфейс:**
- `getAllProducts(): Flow<List<Product>>` - Все товары как Flow
- `getProductById(productId): Product?` - Товар по ID
- `addProduct(product)` - Добавить товар
- `updateProduct(product)` - Обновить товар
- `deleteProduct(productId)` - Удалить товар
- `clearAllProducts()` - Очистить все товары
- `searchProducts(query): Flow<List<Product>>` - Поиск товаров
- `getProductsByStatus(status): Flow<List<Product>>` - Товары по статусу

### 2. `FileRepository.kt` - Репозиторий файлов

**Назначение:** Управление файлами с абстракцией от конкретной реализации.

**Интерфейс:**
- `loadExcelFile(fileUri): Result<ExcelData>` - Загрузка Excel файла
- `saveExcelFile(data, fileName): Result<String>` - Сохранение в Excel
- `getSaveFileUri(fileName): Result<String>` - Получение URI для сохранения
- `isFileAvailable(fileUri): Boolean` - Проверка доступности
- `getFileSize(fileUri): Result<Long>` - Размер файла
- `deleteFile(fileUri): Result<Unit>` - Удаление файла

**Реализация `InMemoryFileRepository`:**
- Заглушка для тестирования (все методы возвращают UnsupportedOperationException)
- Может быть заменена на реальную реализацию с БД или файловой системой

---

## 🔗 Связи между компонентами

```
UI Layer (Jetpack Compose)
         ↓
    ViewModel
         ↓
Repository Layer
    ↙        ↘
Product   File
Repository Repository
    ↓        ↓
  Service Layer
  ↙    ↓    ↘  ↙
Excel File Search Storage
Service Service Service Cell
    ↓    ↓    ↓    ↓
   Data Model Layer
```

**Поток данных:**
1. **Загрузка:** Excel → ExcelService → ExcelData → ProductRepository → ViewModel → UI
2. **Поиск:** UI → SearchService → Filtered Products → UI  
3. **Изменение:** UI → ProductRepository → Updated Product → ExcelService → File
4. **Ячейки:** StorageCellService → Validation → Product Repository → UI

**Архитектурные принципы:**
- **Clean Architecture** - разделение на слои
- **MVVM** - Model-View-ViewModel
- **Repository Pattern** - абстракция доступа к данным
- **Service Layer** - бизнес-логика
- **Flow** - реактивное программирование для UI
- **Result** - безопасная обработка ошибок

---

## 📝 Ключевые особенности

### Excel обработка:
- Многостратегический анализ структуры таблиц
- Интеллектуальный поиск колонок по синонимам
- Исключение служебных строк (договоры, подписи, даты)
- Поддержка fallback логики для сложных структур

### Управление ячейками:
- Уникальная система адресации (Буква+Число1-Число2-Число3)
- Отслеживание занятости ячеек
- Валидация корректности позиций

### Поиск и фильтрация:
- Мультикритериальный поиск
- Реактивные потоки данных
- Гибкие критерии сортировки

### Модель данных:
- Immutable data classes
- Встроенные методы форматирования
- Автоматическая генерация ID
- Связанные объекты (Product ↔ StorageCell)

---

**Документация создана:** 2025-12-16  
**Версия проекта:** Sklad-Ya Android Application