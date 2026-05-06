package com.example.todo.di

import com.example.todo.data.repository.ItemRepositoryImpl
import com.example.todo.data.repository.MarsRepositoryImpl
import com.example.todo.data.repository.TagRepositoryImpl
import com.example.todo.data.repository.TodoRepositoryImpl
import com.example.todo.data.repository.UserRepositoryImpl
import com.example.todo.domain.repository.ItemRepository
import com.example.todo.domain.repository.MarsRepository
import com.example.todo.domain.repository.TagRepository
import com.example.todo.domain.repository.TodoRepository
import com.example.todo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTodoRepository(
        todoRepositoryImpl: TodoRepositoryImpl
    ): TodoRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        itemRepositoryImpl: ItemRepositoryImpl
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ): TagRepository

    @Binds
    @Singleton
    abstract fun bindMarsRepository(
        marsRepositoryImpl: MarsRepositoryImpl
    ): MarsRepository
}
