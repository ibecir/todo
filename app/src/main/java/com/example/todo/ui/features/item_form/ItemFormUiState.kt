package com.example.todo.ui.features.item_form

import com.example.todo.domain.model.Item

sealed interface ItemFormUiState {
    data object Loading : ItemFormUiState
    data class Ready(val existingItem: Item?) : ItemFormUiState
    data object Saving : ItemFormUiState
}
