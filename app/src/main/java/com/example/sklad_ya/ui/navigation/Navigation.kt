package com.example.sklad_ya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sklad_ya.ui.screens.MainScreen

/**
 * Основной граф навигации приложения
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        // TODO: Добавить дополнительные экраны по мере разработки
        // composable(Screen.ProductList.route) { ProductListScreen(navController) }
        // composable(Screen.CellSelection.route) { CellSelectionScreen(navController) }
    }
}

/**
 * Экраны приложения
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object ProductList : Screen("product_list")
    object CellSelection : Screen("cell_selection")
}