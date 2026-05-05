package com.example.todo.model.repository

import com.example.todo.model.dto.CreateTagRequest
import com.example.todo.model.dto.TagDto
import com.example.todo.model.remote.TagsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagsApi: TagsApi
) {
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun getTags(userId: Int): Flow<List<TagDto>> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val allTags = tagsApi.getTags()
                    emit(allTags.filter { it.userId == userId })
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }

    suspend fun createTag(name: String, description: String, userId: Int): TagDto? {
        return try {
            val tag = tagsApi.createTag(CreateTagRequest(name, description, userId))
            refreshTrigger.tryEmit(Unit)
            tag
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteTag(tagId: Int) {
        try {
            tagsApi.deleteTag(tagId)
            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            // ignore for now
        }
    }
}
