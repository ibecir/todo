package com.example.todo.model.repository

import com.example.todo.model.dto.TodoStatsDto
import com.example.todo.model.local.dao.TodoDao
import com.example.todo.model.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(private val todoDao: TodoDao) {
    fun getTodos(userId: Int): Flow<List<TodoEntity>> = todoDao.getAllTodos(userId)
    fun getTodoStats(userId: Int): Flow<TodoStatsDto> = todoDao.getTodoStats(userId)

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)

    suspend fun update(todo: TodoEntity) = todoDao.update(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    fun getTodoById(todoId: Int, userId: Int): Flow<TodoEntity?> = todoDao.getTodoById(todoId, userId)
}
