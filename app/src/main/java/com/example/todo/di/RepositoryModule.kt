package com.example.todo.di

import ba.edu.ibu.BuildConfig
import com.example.todo.data.repository.*
import com.example.todo.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTodoRepository(
        roomImpl: TodoRepositoryImpl,
        firebaseImpl: FirebaseTodoRepositoryImpl
    ): TodoRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl else roomImpl
    }

    @Provides
    @Singleton
    fun provideItemRepository(
        roomImpl: ItemRepositoryImpl,
        firebaseImpl: FirebaseItemRepositoryImpl
    ): ItemRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl else roomImpl
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        roomImpl: UserRepositoryImpl,
        firebaseImpl: FirebaseUserRepositoryImpl
    ): UserRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl else roomImpl
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        roomImpl: TagRepositoryImpl,
        firebaseImpl: FirebaseTagRepositoryImpl
    ): TagRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl else roomImpl
    }

    @Provides
    @Singleton
    fun provideMarsRepository(
        marsRepositoryImpl: MarsRepositoryImpl
    ): MarsRepository = marsRepositoryImpl
}
