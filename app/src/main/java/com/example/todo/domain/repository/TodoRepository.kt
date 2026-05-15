package com.example.todo.domain.repository

import com.example.todo.domain.model.Todo
import com.example.todo.domain.model.TodoStats
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getTodos(userId: Int): Flow<List<Todo>>
    fun getTodoStats(userId: Int): Flow<TodoStats>
    fun getTodoById(todoId: Int, userId: Int): Flow<Todo?>
    suspend fun insert(todo: Todo): Long
    suspend fun update(todo: Todo)
    suspend fun delete(todo: Todo)
}
