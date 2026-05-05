package com.example.todo.model.repository

import com.example.todo.model.dto.ItemStatsDto
import com.example.todo.model.local.dao.ItemDao
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ItemRepository @Inject constructor(private val itemDao: ItemDao) {

    fun getAllItems(userId: Int): Flow<List<ItemEntity>> = itemDao.getAllItems(userId)
    fun getItemStats(userId: Int): Flow<ItemStatsDto> = itemDao.getItemStats(userId)

    fun getItemsForTodo(todoId: Int, userId: Int): Flow<List<ItemEntity>> = itemDao.getItemsForTodo(todoId, userId)

    suspend fun getItemById(itemId: Int, userId: Int): ItemEntity? = itemDao.getItemById(itemId, userId)

    suspend fun insert(item: ItemEntity): Long = itemDao.insert(item)

    suspend fun update(item: ItemEntity) = itemDao.update(item)

    suspend fun delete(item: ItemEntity) = itemDao.delete(item)

    suspend fun addItemToTodo(todoId: Int, itemId: Int) =
        itemDao.addItemToTodo(TodoItems(todoId = todoId, itemId = itemId))

    suspend fun removeItemFromTodo(todoId: Int, itemId: Int) =
        itemDao.removeItemFromTodo(todoId, itemId)
}
