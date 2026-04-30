package com.example.todo.presentation.navigation

sealed class Screen(val route: String) {
    data object TodoList : Screen("todo_list")
    data object AddTodo : Screen("add_todo")
}
