package com.example.todo.ui.features.item_form

sealed interface ItemFormNavigationEvent {
    data object NavigateBack : ItemFormNavigationEvent
}
