package com.example.todo.presentation.view_model.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.model.repository.TodoRepository
import com.example.todo.model.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTodoUiState>(AddTodoUiState.Idle)
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<AddTodoNavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<AddTodoNavigationEvent> = _navigationEvent.receiveAsFlow()

    fun onSaveClick(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _uiState.value = AddTodoUiState.Saving
            repository.insert(
                TodoEntity(
                    title = title.trim(),
                    userId = sessionManager.loggedInUserId
                )
            )
            _navigationEvent.send(AddTodoNavigationEvent.NavigateBack)
        }
    }
}
