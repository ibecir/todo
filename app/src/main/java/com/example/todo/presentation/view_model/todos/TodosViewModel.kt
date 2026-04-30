package com.example.todo.presentation.view_model.todos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.model.repository.ItemRepository
import com.example.todo.model.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodosViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodosUiState())
    val uiState: StateFlow<TodosUiState> = _uiState.asStateFlow()

    private val _selectedTodoId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            todoRepository.todos.collect { todos ->
                _uiState.update { it.copy(
                    todos = todos,
                    selectedTodo = todos.find { t -> t.id == _selectedTodoId.value }
                ) }
            }
        }
        viewModelScope.launch {
            _selectedTodoId.flatMapLatest { id ->
                if (id != null) itemRepository.getItemsForTodo(id) else flowOf(emptyList())
            }.collect { items ->
                _uiState.update { it.copy(selectedTodoItems = items) }
            }
        }
        viewModelScope.launch {
            itemRepository.allItems.collect { items ->
                _uiState.update { it.copy(allItems = items) }
            }
        }
    }

    fun onTodoClick(todo: TodoEntity) {
        _selectedTodoId.value = todo.id
        _uiState.update { it.copy(selectedTodo = todo) }
    }

    fun onCloseSheet() {
        _selectedTodoId.value = null
        _uiState.update { it.copy(selectedTodo = null, selectedTodoItems = emptyList()) }
    }

    fun onToggleItem(item: ItemEntity) {
        val todoId = _selectedTodoId.value ?: return
        val isAssigned = _uiState.value.selectedTodoItems.any { it.id == item.id }
        viewModelScope.launch {
            if (isAssigned) itemRepository.removeItemFromTodo(todoId, item.id)
            else itemRepository.addItemToTodo(todoId, item.id)
        }
    }

    fun onOpenAddTodoDialog() {
        _uiState.update { it.copy(isAddTodoDialogOpen = true) }
    }

    fun onDismissAddTodoDialog() {
        _uiState.update { it.copy(isAddTodoDialogOpen = false) }
    }

    fun onAddTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            todoRepository.insert(TodoEntity(title = title.trim()))
        }
        _uiState.update { it.copy(isAddTodoDialogOpen = false) }
    }

    fun onToggleComplete(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun onDeleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.delete(todo)
        }
    }
}
