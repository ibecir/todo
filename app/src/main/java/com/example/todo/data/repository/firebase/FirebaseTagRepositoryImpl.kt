package com.example.todo.data.repository.firebase

import com.example.todo.data.mapper.toDomain
import com.example.todo.data.mapper.toDto
import com.example.todo.data.remote.dto.TagDto
import com.example.todo.domain.model.Tag
import com.example.todo.domain.repository.TagRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTagRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TagRepository {

    private val tagCollection = firestore.collection("tags")

    override fun getTags(userId: Int): Flow<List<Tag>> {
        return tagCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(TagDto::class.java)?.toDomain() }
            }
    }

    override suspend fun createTag(name: String, description: String, userId: Int): Tag? {
        val tagId = System.currentTimeMillis().toInt()
        val tag = Tag(id = tagId, name = name, description = description, userId = userId)
        tagCollection.add(tag.toDto()).await()
        return tag
    }

    override suspend fun deleteTag(tagId: Int) {
        val snapshot = tagCollection.whereEqualTo("id", tagId).get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }
}
