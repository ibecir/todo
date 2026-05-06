package com.example.todo.domain.repository

import com.example.todo.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTags(userId: Int): Flow<List<Tag>>
    suspend fun createTag(name: String, description: String, userId: Int): Tag?
    suspend fun deleteTag(tagId: Int)
}
