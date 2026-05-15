package com.example.todo.data.remote.services

import androidx.room.Query
import com.example.todo.data.remote.dto.CreateTagRequest
import com.example.todo.data.remote.dto.TagDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TagsApi {

    @Headers("X-Authentication: yes")
    @GET("tags/")
    suspend fun getTags(): List<TagDto>

    @Headers("X-Authentication: yes")
    @POST("tags/")
    suspend fun createTag(@Body request: CreateTagRequest): TagDto

    @Headers("X-Authentication: yes")
    @GET("tags/{tag_id}")
    suspend fun getTag(@Path("tag_id") tagId: Int): TagDto

    @Headers("X-Authentication: yes")
    @PUT("tags/{tag_id}")
    suspend fun updateTag(@Path("tag_id") tagId: Int, @Body request: CreateTagRequest): TagDto

    @Headers("X-Authentication: yes")
    @DELETE("tags/{tag_id}")
    suspend fun deleteTag(@Path("tag_id") tagId: Int)
}
