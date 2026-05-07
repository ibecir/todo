package com.example.todo.di

import ba.edu.ibu.BuildConfig
import com.example.todo.data.repository.firebase.FirebaseItemRepositoryImpl
import com.example.todo.data.repository.firebase.FirebaseTagRepositoryImpl
import com.example.todo.data.repository.firebase.FirebaseTodoRepositoryImpl
import com.example.todo.data.repository.firebase.FirebaseUserRepositoryImpl
import com.example.todo.data.repository.mars.MarsRepositoryImpl
import com.example.todo.data.repository.room.ItemRepositoryImpl
import com.example.todo.data.repository.room.TagRepositoryImpl
import com.example.todo.data.repository.room.TodoRepositoryImpl
import com.example.todo.data.repository.room.UserRepositoryImpl
import com.example.todo.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTodoRepository(
        roomImpl: Provider<TodoRepositoryImpl>,
        firebaseImpl: Provider<FirebaseTodoRepositoryImpl>
    ): TodoRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl.get() else roomImpl.get()
    }

    @Provides
    @Singleton
    fun provideItemRepository(
        roomImpl: Provider<ItemRepositoryImpl>,
        firebaseImpl: Provider<FirebaseItemRepositoryImpl>
    ): ItemRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl.get() else roomImpl.get()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        roomImpl: Provider<UserRepositoryImpl>,
        firebaseImpl: Provider<FirebaseUserRepositoryImpl>
    ): UserRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl.get() else roomImpl.get()
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        roomImpl: Provider<TagRepositoryImpl>,
        firebaseImpl: Provider<FirebaseTagRepositoryImpl>
    ): TagRepository {
        return if (BuildConfig.USE_FIREBASE) firebaseImpl.get() else roomImpl.get()
    }

    @Provides
    @Singleton
    fun provideMarsRepository(
        marsRepositoryImpl: MarsRepositoryImpl
    ): MarsRepository = marsRepositoryImpl
}
