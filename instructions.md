# Инструкции для работы с Android проектом Sklad-Ya

## 🚀 Основные команды сборки

### Сборка проекта
```bash
gradlew build
```
Сборка всего проекта (debug + release + тесты)

### Сборка debug версии
```bash
gradlew assembleDebug
```
Создает debug APK для тестирования

### Сборка release версии  
```bash
gradlew assembleRelease
```
Создает unsigned release APK для публикации

### Сборка и запуск тестов
```bash
gradlew test
gradlew connectedAndroidTest
```

### Очистка проекта
```bash
gradlew clean
```
Удаляет все файлы сборки из `app/build/`

## 📱 Установка и запуск на устройстве

### Установка debug APK
```bash
gradlew installDebug
```

### Запуск приложения на подключенном устройстве
```bash
gradlew installDebug
adb shell am start -n com.example.sklad_ya/.MainActivity
```

### Проверка подключенных устройств
```bash
adb devices
```

## 🔍 Анализ и отладка

### Линтинг кода
```bash
gradlew lint
```

### Проверка зависимостей
```bash
gradlew app:dependencies
```

### Анализ размера APK
```bash
gradlew app:assembleDebug --info | grep -i "size"
```

## 📊 Работа с файлами проекта

### Структура проекта
```
sklad-ya/
├── app/src/main/
│   ├── java/com/example/sklad_ya/
│   │   ├── MainActivity.kt
│   │   ├── data/
│   │   │   ├── model/          # Модели данных
│   │   │   ├── service/        # Сервисы
│   │   │   └── repository/     # Репозитории
│   │   └── ui/                 # UI компоненты
│   ├── res/                    # Ресурсы
│   └── AndroidManifest.xml
├── gradle/                     # Конфигурация Gradle
└── build.gradle.kts           # Основной файл сборки
```

### Важные файлы для проверки
- `local.properties` - путь к Android SDK
- `gradle.properties` - настройки проекта
- `app/build.gradle.kts` - зависимости и конфигурация приложения

## 🎯 Команды для быстрой работы

### Обновление зависимостей
```bash
gradlew build --refresh-dependencies
```

### Сборка конкретного варианта
```bash
gradlew assembleDebug
gradlew assembleRelease  
gradlew bundleRelease
```

### Проверка сборки
```bash
gradlew check
```
Выполняет все проверки (линтинг, тесты)

## 📋 Типичные задачи

### 1. Подготовка к тестированию
```bash
gradlew clean assembleDebug
```

### 2. Создание release версии
```bash
gradlew clean assembleRelease
```

### 3. Полная проверка проекта
```bash
gradlew clean build
```

### 4. Установка на устройство
```bash
gradlew installDebug
```

## 🔧 Решение проблем

### Ошибки синхронизации Gradle
- Убедитесь что SDK путь корректен в `local.properties`
- Очистите кеш: `gradlew clean build --refresh-dependencies`

### Ошибки сборки
- Проверьте версии зависимостей в `build.gradle.kts`
- Синхронизируйте проект с Gradle файлами

### Проблемы с подключением устройства
```bash
adb kill-server
adb start-server
adb devices
```

## 📝 Заметки

- Debug APK создается автоматически подписанным для разработки
- Release APK требует подписи перед публикацией
- Файлы сборки находятся в `app/build/outputs/`
- Логи приложения: `adb logcat`

---

**Последнее обнов5-12-ление:** 202ект:** Sklad16  
**Про-Ya Android Application