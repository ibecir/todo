package com.example.todo.ui.features.todo_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.model.Item
import com.example.todo.domain.repository.ItemRepository
import com.example.todo.domain.repository.TagRepository
import com.example.todo.domain.repository.TodoRepository
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
class TodoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository,
    private val itemRepository: ItemRepository,
    private val tagRepository: TagRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val todoId: Int = checkNotNull(savedStateHandle["todoId"])

    private val _uiState = MutableStateFlow<TodoDetailUiState>(TodoDetailUiState.Loading)
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<TodoDetailNavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<TodoDetailNavigationEvent> = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) {
                    combine(
                        todoRepository.getTodoById(todoId, userId),
                        itemRepository.getItemsForTodo(todoId, userId),
                        tagRepository.getTags(userId)
                    ) { todo, items, allTags ->
                        todo?.let {
                            val todoTags = allTags.filter { tag -> tag.id in it.tagIds }
                            TodoDetailUiState.Success(it, items, todoTags, allTags)
                        } ?: TodoDetailUiState.Loading
                    }
                } else {
                    flowOf(TodoDetailUiState.Loading)
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onAddItemClick() {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateToItemForm(todoId, null))
        }
    }

    fun onEditItemClick(item: Item) {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateToItemForm(todoId, item.id))
        }
    }

    fun onDeleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.delete(item)
        }
    }

    fun onToggleTag(tagId: Int) {
        val currentState = _uiState.value
        if (currentState is TodoDetailUiState.Success) {
            val todo = currentState.todo
            val newTagIds = if (todo.tagIds.contains(tagId)) {
                todo.tagIds - tagId
            } else {
                todo.tagIds + tagId
            }
            viewModelScope.launch {
                todoRepository.update(todo.copy(tagIds = newTagIds))
            }
        }
    }

    fun onCreateTag(name: String, description: String) {
        viewModelScope.launch {
            val userId = sessionManager.loggedInUserId
            if (userId != -1) {
                tagRepository.createTag(name, description, userId)
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navigationEvent.send(TodoDetailNavigationEvent.NavigateBack)
        }
    }
}
