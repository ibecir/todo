package com.example.todo.presentation.view_model.list

import com.example.todo.model.local.entity.TodoEntity

sealed interface TodoListUiState {
    data object Loading : TodoListUiState
    data class Success(val todos: List<TodoEntity>) : TodoListUiState
}
