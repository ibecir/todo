package com.example.todo.presentation.view_model.item_form

sealed interface ItemFormNavigationEvent {
    data object NavigateBack : ItemFormNavigationEvent
}
