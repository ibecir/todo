package com.example.todo.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    val isLoggedIn: StateFlow<Boolean> = sessionManager.userId
        .map { it != -1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionManager.isLoggedIn)

    val loggedInUsername: StateFlow<String?> = sessionManager.username
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionManager.loggedInUsername)

    val profilePictureUrl: StateFlow<String?> = sessionManager.profilePictureUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), sessionManager.loggedInProfilePictureUrl)

    fun onToggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null) }
    }

    fun onLogin(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.login(username, password)
            result.onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
    }

    fun onRegister(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.register(username, password)
            result.onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = userRepository.signInWithGoogle(idToken)
            result.onFailure { e -> 
                android.util.Log.e("AuthViewModel", "Google Sign-In failed", e)
                _uiState.update { it.copy(errorMessage = e.message) } 
            }
            result.onSuccess {
                android.util.Log.d("AuthViewModel", "Google Sign-In successful")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
