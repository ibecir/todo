package com.example.todo.ui.features.todos

import com.example.todo.domain.model.Item
import com.example.todo.domain.model.Tag
import com.example.todo.domain.model.Todo

data class TodosUiState(
    val todos: List<Todo> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedTodo: Todo? = null,
    val selectedTodoItems: List<Item> = emptyList(),
    val allItems: List<Item> = emptyList(),
    val isAddTodoDialogOpen: Boolean = false
)
