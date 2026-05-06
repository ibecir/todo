package com.example.todo.ui.features.todo_detail

sealed interface TodoDetailNavigationEvent {
    data object NavigateBack : TodoDetailNavigationEvent
    data class NavigateToItemForm(val todoId: Int, val itemId: Int?) : TodoDetailNavigationEvent
}
