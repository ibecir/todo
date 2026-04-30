package com.example.todo.di

import android.content.Context
import androidx.room.Room
import com.example.todo.model.local.TodoDatabase
import com.example.todo.model.local.dao.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Singleton missing
// State with auth
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TodoDatabase =
        Room.databaseBuilder(context, TodoDatabase::class.java, "todo_database").build()

    @Provides
    fun provideTodoDao(db: TodoDatabase): TodoDao = db.todoDao()
}
