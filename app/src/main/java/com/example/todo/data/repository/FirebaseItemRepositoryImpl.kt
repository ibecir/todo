package com.example.todo.data.repository

import com.example.todo.data.mapper.toDomain
import com.example.todo.data.mapper.toFirebaseDto
import com.example.todo.data.remote.dto.FirebaseItemDto
import com.example.todo.domain.model.Item
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.repository.ItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {

    private val itemCollection = firestore.collection("items")
    private val todoItemsCollection = firestore.collection("todo_items")

    override fun getAllItems(userId: Int): Flow<List<Item>> {
        return itemCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(FirebaseItemDto::class.java)?.toDomain() }
            }
    }

    override fun getItemStats(userId: Int): Flow<ItemStats> {
        return combine(
            getAllItems(userId),
            todoItemsCollection.snapshots()
        ) { items, todoItemsSnapshot ->
            val total = items.size
            val assigned = todoItemsSnapshot.documents.size
            ItemStats(totalCount = total, assignedCount = assigned)
        }
    }

    override fun getItemsForTodo(todoId: Int, userId: Int): Flow<List<Item>> {
        return todoItemsCollection
            .whereEqualTo("todoId", todoId)
            .snapshots()
            .map { snapshot ->
                val itemIds = snapshot.documents.mapNotNull { it.getLong("itemId")?.toInt() }
                if (itemIds.isEmpty()) return@map emptyList<Item>()
                
                itemCollection
                    .whereIn("id", itemIds)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(FirebaseItemDto::class.java)?.toDomain() }
            }
    }

    override suspend fun getItemById(itemId: Int, userId: Int): Item? {
        return itemCollection
            .whereEqualTo("id", itemId)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(FirebaseItemDto::class.java)
            ?.toDomain()
    }

    override suspend fun insert(item: Item): Long {
        val newItemId = System.currentTimeMillis()
        val dto = item.copy(id = newItemId.toInt()).toFirebaseDto()
        itemCollection.add(dto).await()
        return newItemId
    }

    override suspend fun update(item: Item) {
        val snapshot = itemCollection.whereEqualTo("id", item.id).get().await()
        snapshot.documents.forEach { it.reference.set(item.toFirebaseDto()).await() }
    }

    override suspend fun delete(item: Item) {
        val snapshot = itemCollection.whereEqualTo("id", item.id).get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    override suspend fun addItemToTodo(todoId: Int, itemId: Int) {
        val exists = todoItemsCollection
            .whereEqualTo("todoId", todoId)
            .whereEqualTo("itemId", itemId)
            .get()
            .await()
        
        if (exists.isEmpty) {
            todoItemsCollection.add(mapOf("todoId" to todoId, "itemId" to itemId)).await()
        }
    }

    override suspend fun removeItemFromTodo(todoId: Int, itemId: Int) {
        val snapshot = todoItemsCollection
            .whereEqualTo("todoId", todoId)
            .whereEqualTo("itemId", itemId)
            .get()
            .await()
        
        snapshot.documents.forEach { it.reference.delete().await() }
    }
}
