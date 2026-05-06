package com.example.todo.data.remote

import com.example.todo.data.remote.dto.MarsPhotoDto
import retrofit2.http.GET

interface MarsApiService {
    @GET("photos")
    suspend fun getPhotos(): List<MarsPhotoDto>
}
