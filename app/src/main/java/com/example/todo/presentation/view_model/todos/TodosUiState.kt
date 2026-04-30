package com.example.todo.presentation.view_model.todos

import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity

data class TodosUiState(
    val todos: List<TodoEntity> = emptyList(),
    val selectedTodo: TodoEntity? = null,
    val selectedTodoItems: List<ItemEntity> = emptyList(),
    val allItems: List<ItemEntity> = emptyList(),
    val isAddTodoDialogOpen: Boolean = false
)
