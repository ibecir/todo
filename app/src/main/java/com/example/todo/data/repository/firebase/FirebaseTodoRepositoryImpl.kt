package com.example.todo.data.repository.firebase

import com.example.todo.data.local.entity.TodoEntity
import com.example.todo.data.mapper.toDomain
import com.example.todo.data.mapper.toEntity
import com.example.todo.domain.model.Todo
import com.example.todo.domain.model.TodoStats
import com.example.todo.domain.repository.TodoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTodoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TodoRepository {

    private val todoCollection = firestore.collection("todos")

    override fun getTodos(userId: Int): Flow<List<Todo>> {
        return todoCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(TodoEntity::class.java)?.toDomain() }
            }
    }

    override fun getTodoStats(userId: Int): Flow<TodoStats> {
        return getTodos(userId).map { todos ->
            val total = todos.size
            val completed = todos.count { it.isCompleted }
            TodoStats(
                totalCount = total,
                completedCount = completed,
                pendingCount = total - completed
            )
        }
    }

    override fun getTodoById(todoId: Int, userId: Int): Flow<Todo?> {
        return todoCollection
            .whereEqualTo("id", todoId)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { it.documents.firstOrNull()?.toObject(TodoEntity::class.java)?.toDomain() }
    }

    override suspend fun insert(todo: Todo): Long {
        val newTodoId = System.currentTimeMillis()
        val entity = todo.copy(id = newTodoId.toInt()).toEntity()
        todoCollection.add(entity).await()
        return newTodoId
    }

    override suspend fun update(todo: Todo) {
        val snapshot = todoCollection
            .whereEqualTo("id", todo.id)
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            doc.reference.set(todo.toEntity()).await()
        }
    }

    override suspend fun delete(todo: Todo) {
        val snapshot = todoCollection
            .whereEqualTo("id", todo.id)
            .get()
            .await()
        
        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }
}
