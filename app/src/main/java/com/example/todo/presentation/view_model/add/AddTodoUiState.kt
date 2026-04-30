package com.example.todo.presentation.view_model.add

sealed interface AddTodoUiState {
    data object Idle : AddTodoUiState
    data object Saving : AddTodoUiState
}
