package com.example.todo.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todo.model.local.dao.ItemDao
import com.example.todo.model.local.dao.TodoDao
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.model.local.entity.TodoItemCrossRef

@Database(
    entities = [TodoEntity::class, ItemEntity::class, TodoItemCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun itemDao(): ItemDao
}
