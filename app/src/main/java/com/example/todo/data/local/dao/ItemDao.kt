package com.example.todo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todo.data.remote.dto.ItemStatsDto
import com.example.todo.data.local.entity.ItemEntity
import com.example.todo.data.local.entity.TodoItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllItems(userId: Int): Flow<List<ItemEntity>>

    @Query("SELECT i.* FROM items i INNER JOIN todo_items cr ON i.id = cr.itemId WHERE cr.todoId = :todoId AND i.userId = :userId ORDER BY i.createdAt DESC")
    fun getItemsForTodo(todoId: Int, userId: Int): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId AND userId = :userId")
    suspend fun getItemById(itemId: Int, userId: Int): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("""
        SELECT
            COUNT(*) AS totalCount,
            (SELECT COUNT(DISTINCT itemId) FROM todo_items cr INNER JOIN items i ON cr.itemId = i.id WHERE i.userId = :userId) AS assignedCount
        FROM items
        WHERE userId = :userId
    """)
    fun getItemStats(userId: Int): Flow<ItemStatsDto>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItemToTodo(crossRef: TodoItems)

    @Query("DELETE FROM todo_items WHERE todoId = :todoId AND itemId = :itemId")
    suspend fun removeItemFromTodo(todoId: Int, itemId: Int)
}
