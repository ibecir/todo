package com.example.todo.presentation.view_model.add

sealed interface AddTodoNavigationEvent {
    data object NavigateBack : AddTodoNavigationEvent
}
