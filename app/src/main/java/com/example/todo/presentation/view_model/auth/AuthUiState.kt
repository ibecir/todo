package com.example.todo.presentation.view_model.auth

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
