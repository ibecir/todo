package com.example.todo.model.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todo.model.dto.TodoStatsDto
import com.example.todo.model.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos WHERE userId = :userId ORDER BY id DESC")
    fun getAllTodos(userId: Int): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("SELECT * FROM todos WHERE id = :todoId AND userId = :userId")
    fun getTodoById(todoId: Int, userId: Int): Flow<TodoEntity?>

    @Query("""
        SELECT
            COUNT(*) AS totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) AS completedCount,
            SUM(CASE WHEN isCompleted = 0 THEN 1 ELSE 0 END) AS pendingCount
        FROM todos
        WHERE userId = :userId
    """)
    fun getTodoStats(userId: Int): Flow<TodoStatsDto>
}
