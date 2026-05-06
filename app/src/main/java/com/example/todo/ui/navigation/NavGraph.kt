package com.example.todo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todo.ui.features.MainScreen
import com.example.todo.ui.features.item_form.ItemFormScreen
import com.example.todo.ui.features.todo_detail.TodoDetailScreen

@Composable
fun NavGraph(navController: NavHostController, username: String?, onLogout: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                username = username,
                onLogout = onLogout
                // If we want to navigate from MainScreen to Detail, 
                // we'd pass navigation lambdas here.
                // For now, MainScreen is self-contained with its tabs.
            )
        }

        composable(
            route = Screen.TodoDetail.route,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) {
            TodoDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemForm = { todoId, itemId ->
                    navController.navigate(Screen.ItemForm.createRoute(todoId, itemId))
                }
            )
        }

        composable(
            route = Screen.ItemForm.route,
            arguments = listOf(
                navArgument("todoId") { type = NavType.IntType },
                navArgument("itemId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            ItemFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
