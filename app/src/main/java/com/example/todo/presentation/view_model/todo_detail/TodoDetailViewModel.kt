package com.example.todo.presentation.view_model.todo_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.repository.ItemRepository
import com.example.todo.model.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val todoId: Int = checkNotNull(savedStateHandle["todoId"])

    private val _uiState = MutableStateFlow<TodoDetailUiState>(TodoDetailUiState.Loading)
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<TodoDetailNavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<TodoDetailNavigationEvent> = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                todoRepository.getTodoById(todoId),
                itemRepository.getItemsForTodo(todoId)
            ) { todo, items ->
                todo?.let { TodoDetailUiState.Success(it, items) } ?: TodoDetailUiState.Loading
            }.collect { _uiState.value = it }
        }
    }

    fun onAddItemClick() {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateToItemForm(todoId, null))
        }
    }

    fun onEditItemClick(item: ItemEntity) {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateToItemForm(todoId, item.id))
        }
    }

    fun onDeleteItem(item: ItemEntity) {
        viewModelScope.launch {
            itemRepository.delete(item)
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateBack)
        }
    }
}
