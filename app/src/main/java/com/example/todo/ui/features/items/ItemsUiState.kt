package com.example.todo.ui.features.items

import com.example.todo.domain.model.Item

data class ItemsUiState(
    val items: List<Item> = emptyList(),
    val isDialogOpen: Boolean = false,
    val editingItem: Item? = null
)
