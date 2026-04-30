package com.example.todo.presentation.view_model.list

sealed interface TodoListNavigationEvent {
    data object NavigateToAdd : TodoListNavigationEvent
    data class NavigateToDetail(val todoId: Int) : TodoListNavigationEvent
}
