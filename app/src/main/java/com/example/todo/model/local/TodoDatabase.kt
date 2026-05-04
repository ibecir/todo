package com.example.todo.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todo.model.local.dao.ItemDao
import com.example.todo.model.local.dao.TodoDao
import com.example.todo.model.local.dao.UserDao
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.model.local.entity.TodoItemCrossRef
import com.example.todo.model.local.entity.UserEntity

@Database(
    entities = [TodoEntity::class, ItemEntity::class, TodoItemCrossRef::class, UserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun itemDao(): ItemDao
    abstract fun userDao(): UserDao
}
