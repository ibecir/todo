package com.example.todo.presentation.view_model.item_form

import com.example.todo.model.local.entity.ItemEntity

sealed interface ItemFormUiState {
    data object Loading : ItemFormUiState
    data class Ready(val existingItem: ItemEntity? = null) : ItemFormUiState
    data object Saving : ItemFormUiState
}
