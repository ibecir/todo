package com.example.todo.presentation.view_model.items

import com.example.todo.model.local.entity.ItemEntity

data class ItemsUiState(
    val items: List<ItemEntity> = emptyList(),
    val isDialogOpen: Boolean = false,
    val editingItem: ItemEntity? = null  // null = add mode when dialog is open
)
