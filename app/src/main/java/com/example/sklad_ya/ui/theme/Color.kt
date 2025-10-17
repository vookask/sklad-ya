package com.example.sklad_ya.ui.theme

import androidx.compose.ui.graphics.Color

// Темная тема в стиле веб-версии
val DarkBackground = Color(0xFF0f172a)      // Основной фон
val DarkCard = Color(0xFF111827)           // Карточки и поверхности
val DarkInk = Color(0xFFE5E7EB)            // Основной текст
val DarkMuted = Color(0xFF9CA3AF)          // Второстепенный текст
val AccentGreen = Color(0xFF22C55E)        // Акцент (зеленый)
val WarningRed = Color(0xFFEF4444)         // Предупреждение (красный)
val DarkBorder = Color(0xFF1F2937)         // Границы
val DarkSurface = Color(0xFF0B1220)        // Поверхности форм

// Статусы товаров
val StatusOk = Color(0xFF22C55E)           // ✓ Совпадает
val StatusWarn = Color(0xFFF59E0B)         // ⚠ Не совпадает
val StatusPending = Color(0xFF6B7280)       // Ожидает

// Material 3 цветовая схема для темной темы
val md_theme_dark_primary = AccentGreen
val md_theme_dark_onPrimary = Color.White
val md_theme_dark_primaryContainer = Color(0xFF004A1A)
val md_theme_dark_onPrimaryContainer = Color(0xFF7BFF8C)

val md_theme_dark_secondary = Color(0xFFB8C9B8)
val md_theme_dark_onSecondary = Color(0xFF243424)
val md_theme_dark_secondaryContainer = Color(0xFF3A4A3A)
val md_theme_dark_onSecondaryContainer = Color(0xFFD4E5D4)

val md_theme_dark_tertiary = Color(0xFFA0C9FF)
val md_theme_dark_onTertiary = Color(0xFF003257)
val md_theme_dark_tertiaryContainer = Color(0xFF00497D)
val md_theme_dark_onTertiaryContainer = Color(0xFFD0E4FF)

val md_theme_dark_error = WarningRed
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color.White
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = DarkBackground
val md_theme_dark_onBackground = DarkInk
val md_theme_dark_surface = DarkCard
val md_theme_dark_onSurface = DarkInk
val md_theme_dark_surfaceVariant = Color(0xFF414941)
val md_theme_dark_onSurfaceVariant = DarkMuted
val md_theme_dark_outline = DarkBorder
val md_theme_dark_inverseOnSurface = Color.Black
val md_theme_dark_inverseSurface = Color(0xFFE1E3DD)
val md_theme_dark_inversePrimary = Color(0xFF006E1C)
val md_theme_dark_surfaceTint = AccentGreen