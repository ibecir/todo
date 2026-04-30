package com.example.todo.model.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todo.model.dto.ItemStatsDto
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoItemCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT i.* FROM items i INNER JOIN todo_item_cross_ref cr ON i.id = cr.itemId WHERE cr.todoId = :todoId ORDER BY i.createdAt DESC")
    fun getItemsForTodo(todoId: Int): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("""
        SELECT
            COUNT(*) AS totalCount,
            (SELECT COUNT(DISTINCT itemId) FROM todo_item_cross_ref) AS assignedCount
        FROM items
    """)
    fun getItemStats(): Flow<ItemStatsDto>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItemToTodo(crossRef: TodoItemCrossRef)

    @Delete
    suspend fun removeItemFromTodo(crossRef: TodoItemCrossRef)
}
