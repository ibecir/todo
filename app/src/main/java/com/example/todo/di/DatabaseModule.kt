package com.example.todo.di

import android.content.Context
import androidx.room.Room
import com.example.todo.data.local.TodoDatabase
import com.example.todo.data.local.dao.ItemDao
import com.example.todo.data.local.dao.TodoDao
import com.example.todo.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// State with auth
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TodoDatabase =
        Room.databaseBuilder(context, TodoDatabase::class.java, "todo_database")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    @Singleton
    fun provideTodoDao(db: TodoDatabase): TodoDao = db.todoDao()

    @Provides
    @Singleton
    fun provideItemDao(db: TodoDatabase): ItemDao = db.itemDao()

    @Provides
    @Singleton
    fun provideUserDao(db: TodoDatabase): UserDao = db.userDao()
}
