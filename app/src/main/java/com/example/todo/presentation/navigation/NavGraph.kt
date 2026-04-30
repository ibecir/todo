package com.example.todo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todo.presentation.ui.screens.AddTodoScreen
import com.example.todo.presentation.ui.screens.ItemFormScreen
import com.example.todo.presentation.ui.screens.TodoDetailScreen
import com.example.todo.presentation.ui.screens.TodoListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddTodo.route) },
                onNavigateToDetail = { todoId ->
                    navController.navigate(Screen.TodoDetail.createRoute(todoId))
                }
            )
        }

        composable(Screen.AddTodo.route) {
            AddTodoScreen(
                onNavigateBack = { navController.popBackStack() }
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
