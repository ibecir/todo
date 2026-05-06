package com.example.todo.data.repository

import com.example.todo.data.mapper.toDomain
import com.example.todo.data.remote.TagsApi
import com.example.todo.data.remote.dto.CreateTagRequest
import com.example.todo.domain.model.Tag
import com.example.todo.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagsApi: TagsApi
) : TagRepository {
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun getTags(userId: Int): Flow<List<Tag>> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val allTags = tagsApi.getTags()
                    emit(allTags.filter { it.userId == userId }.map { it.toDomain() })
                } catch (e: IOException) {
                    emit(emptyList())
                } catch (e: HttpException) {
                    emit(emptyList())
                }
            }
        }

    override suspend fun createTag(name: String, description: String, userId: Int): Tag? {
        return try {
            val tag = tagsApi.createTag(CreateTagRequest(name, description, userId))
            refreshTrigger.tryEmit(Unit)
            tag.toDomain()
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
    }

    override suspend fun deleteTag(tagId: Int) {
        try {
            tagsApi.deleteTag(tagId)
            refreshTrigger.tryEmit(Unit)
        } catch (e: IOException) {
            // ignore for now
        } catch (e: HttpException) {
            // ignore for now
        }
    }
}
