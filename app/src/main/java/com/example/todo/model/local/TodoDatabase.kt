package com.example.todo.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todo.model.local.dao.TodoDao
import com.example.todo.model.local.entity.TodoEntity

@Database(entities = [TodoEntity::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
