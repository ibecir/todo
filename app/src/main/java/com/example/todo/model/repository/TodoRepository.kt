package com.example.todo.model.repository

import com.example.todo.model.dto.TodoStatsDto
import com.example.todo.model.local.dao.TodoDao
import com.example.todo.model.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()
    val todoStats: Flow<TodoStatsDto> = todoDao.getTodoStats()

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)

    suspend fun update(todo: TodoEntity) = todoDao.update(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    fun getTodoById(todoId: Int): Flow<TodoEntity?> = todoDao.getTodoById(todoId)
}
