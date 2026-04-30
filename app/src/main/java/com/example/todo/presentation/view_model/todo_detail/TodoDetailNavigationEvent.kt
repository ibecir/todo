package com.example.todo.presentation.view_model.todo_detail

sealed interface TodoDetailNavigationEvent {
    data object NavigateBack : TodoDetailNavigationEvent
    data class NavigateToItemForm(val todoId: Int, val itemId: Int?) : TodoDetailNavigationEvent
}
