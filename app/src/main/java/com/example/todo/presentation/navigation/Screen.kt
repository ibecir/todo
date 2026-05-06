package com.example.todo.presentation.navigation

sealed class Screen(val route: String) {
    data object TodoList : Screen("todo_list")
    data object AddTodo : Screen("add_todo")
    data object TodoDetail : Screen("todo_detail/{todoId}") {
        fun createRoute(todoId: Int) = "todo_detail/$todoId"
    }
    data object ItemForm : Screen("item_form/{todoId}?itemId={itemId}") {
        fun createRoute(todoId: Int, itemId: Int? = null) =
            if (itemId != null) "item_form/$todoId?itemId=$itemId"
            else "item_form/$todoId"
    }
    data object MarsPhotos : Screen("mars_photos")
}
