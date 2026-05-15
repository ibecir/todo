package com.example.todo.ui.features.todo_detail

import com.example.todo.domain.model.Item
import com.example.todo.domain.model.Tag
import com.example.todo.domain.model.Todo

sealed interface TodoDetailUiState {
    data object Loading : TodoDetailUiState
    data class Success(
        val todo: Todo,
        val items: List<Item>,
        val tags: List<Tag>,
        val allAvailableTags: List<Tag>
    ) : TodoDetailUiState
}
