package com.example.todo.presentation.view_model.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.repository.UserRepository
import com.example.todo.model.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUsername = MutableStateFlow(sessionManager.loggedInUsername)
    val loggedInUsername: StateFlow<String?> = _loggedInUsername.asStateFlow()

    fun onToggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null) }
    }

    fun onLogin(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.login(username, password)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _loggedInUsername.value = username
                },
                onFailure = { e -> _uiState.update { it.copy(errorMessage = e.message) } }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
        _isLoggedIn.value = false
        _loggedInUsername.value = null
    }

    fun onRegister(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.register(username, password)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _loggedInUsername.value = username
                },
                onFailure = { e -> _uiState.update { it.copy(errorMessage = e.message) } }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
