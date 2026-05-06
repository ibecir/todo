package com.example.todo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todo.data.local.dao.ItemDao
import com.example.todo.data.local.dao.TodoDao
import com.example.todo.data.local.dao.UserDao
import com.example.todo.data.local.entity.ItemEntity
import com.example.todo.data.local.entity.TodoEntity
import com.example.todo.data.local.entity.TodoItems
import com.example.todo.data.local.entity.UserEntity

@Database(
    entities = [TodoEntity::class, ItemEntity::class, TodoItems::class, UserEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun itemDao(): ItemDao
    abstract fun userDao(): UserDao
}
