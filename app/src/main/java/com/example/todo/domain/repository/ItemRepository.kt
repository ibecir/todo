package com.example.todo.domain.repository

import com.example.todo.domain.model.Item
import com.example.todo.domain.model.ItemStats
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getAllItems(userId: Int): Flow<List<Item>>
    fun getItemStats(userId: Int): Flow<ItemStats>
    fun getItemsForTodo(todoId: Int, userId: Int): Flow<List<Item>>
    suspend fun getItemById(itemId: Int, userId: Int): Item?
    suspend fun insert(item: Item): Long
    suspend fun update(item: Item)
    suspend fun delete(item: Item)
    suspend fun addItemToTodo(todoId: Int, itemId: Int)
    suspend fun removeItemFromTodo(todoId: Int, itemId: Int)
}
