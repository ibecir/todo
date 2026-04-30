package com.example.todo.model.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TodoWithItems(
    @Embedded val todo: TodoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TodoItemCrossRef::class,
            parentColumn = "todoId",
            entityColumn = "itemId"
        )
    )
    val items: List<ItemEntity>
)
