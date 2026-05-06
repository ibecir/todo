package com.example.todo.data.repository

import com.example.todo.data.local.dao.ItemDao
import com.example.todo.data.local.entity.TodoItems
import com.example.todo.data.mapper.toDomain
import com.example.todo.data.mapper.toEntity
import com.example.todo.domain.model.Item
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getAllItems(userId: Int): Flow<List<Item>> = 
        itemDao.getAllItems(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getItemStats(userId: Int): Flow<ItemStats> = 
        itemDao.getItemStats(userId).map { it.toDomain() }

    override fun getItemsForTodo(todoId: Int, userId: Int): Flow<List<Item>> = 
        itemDao.getItemsForTodo(todoId, userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getItemById(itemId: Int, userId: Int): Item? = 
        itemDao.getItemById(itemId, userId)?.toDomain()

    override suspend fun insert(item: Item): Long = 
        itemDao.insert(item.toEntity())

    override suspend fun update(item: Item) {
        itemDao.update(item.toEntity())
    }

    override suspend fun delete(item: Item) {
        itemDao.delete(item.toEntity())
    }

    override suspend fun addItemToTodo(todoId: Int, itemId: Int) {
        itemDao.addItemToTodo(TodoItems(todoId = todoId, itemId = itemId))
    }

    override suspend fun removeItemFromTodo(todoId: Int, itemId: Int) {
        itemDao.removeItemFromTodo(todoId, itemId)
    }
}
