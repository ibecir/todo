package com.example.todo.di

import com.example.todo.model.remote.MarsApiService
import com.example.todo.model.remote.TagsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MarsRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8000/"
    private const val MARS_BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @MarsRetrofit
    fun provideMarsRetrofit(json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MARS_BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideTagsApi(retrofit: Retrofit): TagsApi {
        return retrofit.create(TagsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMarsApiService(@MarsRetrofit retrofit: Retrofit): MarsApiService {
        return retrofit.create(MarsApiService::class.java)
    }
}
