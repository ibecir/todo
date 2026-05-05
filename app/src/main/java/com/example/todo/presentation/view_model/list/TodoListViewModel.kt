package com.example.todo.presentation.view_model.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.model.repository.TodoRepository
import com.example.todo.model.repository.TagRepository
import com.example.todo.model.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val tagRepository: TagRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodoListUiState>(TodoListUiState.Loading)
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<TodoListNavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<TodoListNavigationEvent> = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) {
                    combine(
                        repository.getTodos(userId),
                        tagRepository.getTags(userId)
                    ) { todos, tags ->
                        TodoListUiState.Success(todos, tags)
                    }
                } else {
                    flowOf(TodoListUiState.Loading)
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onAddClick() {
        viewModelScope.launch {
            _navigationEvent.send(TodoListNavigationEvent.NavigateToAdd)
        }
    }

    fun onTodoClick(todo: TodoEntity) {
        viewModelScope.launch {
            _navigationEvent.send(TodoListNavigationEvent.NavigateToDetail(todo.id))
        }
    }

    fun onToggleComplete(todo: TodoEntity) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun onDelete(todo: TodoEntity) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }
}
