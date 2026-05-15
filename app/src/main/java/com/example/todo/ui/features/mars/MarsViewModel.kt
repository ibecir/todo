package com.example.todo.ui.features.mars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.domain.model.MarsPhoto
import com.example.todo.domain.repository.MarsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MarsUiState {
    data class Success(val photos: List<MarsPhoto>) : MarsUiState
    data class Error(val message: String) : MarsUiState
    data object Loading : MarsUiState
}

@HiltViewModel
class MarsViewModel @Inject constructor(
    private val marsRepository: MarsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarsUiState>(MarsUiState.Loading)
    val uiState: StateFlow<MarsUiState> = _uiState.asStateFlow()

    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            _uiState.update { MarsUiState.Loading }
            try {
                val listResult = marsRepository.getMarsPhotos()
                _uiState.update { MarsUiState.Success(listResult) }
            } catch (e: Exception) {
                _uiState.update { MarsUiState.Error(e.localizedMessage ?: "Unknown error") }
            }
        }
    }
}
