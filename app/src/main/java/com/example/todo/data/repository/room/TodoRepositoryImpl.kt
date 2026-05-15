package com.example.todo.data.repository.room

import com.example.todo.data.local.dao.TodoDao
import com.example.todo.data.mapper.toDomain
import com.example.todo.data.mapper.toEntity
import com.example.todo.domain.model.Todo
import com.example.todo.domain.model.TodoStats
import com.example.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {
    
    override fun getTodos(userId: Int): Flow<List<Todo>> = 
        todoDao.getAllTodos(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getTodoStats(userId: Int): Flow<TodoStats> = 
        todoDao.getTodoStats(userId).map { it.toDomain() }

    override fun getTodoById(todoId: Int, userId: Int): Flow<Todo?> = 
        todoDao.getTodoById(todoId, userId).map { it?.toDomain() }

    override suspend fun insert(todo: Todo): Long = 
        todoDao.insert(todo.toEntity())

    override suspend fun update(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    override suspend fun delete(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }
}
