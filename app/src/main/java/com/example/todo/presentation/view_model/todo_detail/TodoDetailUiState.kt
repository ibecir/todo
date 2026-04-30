package com.example.todo.presentation.view_model.todo_detail

import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity

sealed interface TodoDetailUiState {
    data object Loading : TodoDetailUiState
    data class Success(
        val todo: TodoEntity,
        val items: List<ItemEntity>
    ) : TodoDetailUiState
}
