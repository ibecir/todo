package com.example.todo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todo.presentation.ui.screens.AddTodoScreen
import com.example.todo.presentation.ui.screens.TodoListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddTodo.route) }
            )
        }
        composable(Screen.AddTodo.route) {
            AddTodoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
