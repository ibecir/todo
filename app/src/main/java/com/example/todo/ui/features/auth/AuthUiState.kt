package com.example.todo.ui.features.auth

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
