package com.example.todo.data.mapper

import com.example.todo.data.local.entity.ItemEntity
import com.example.todo.data.local.entity.TodoEntity
import com.example.todo.data.local.entity.UserEntity
import com.example.todo.data.remote.dto.ItemStatsDto
import com.example.todo.data.remote.dto.MarsPhotoDto
import com.example.todo.data.remote.dto.TagDto
import com.example.todo.data.remote.dto.TodoStatsDto
import com.example.todo.domain.model.Item
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.Tag
import com.example.todo.domain.model.Todo
import com.example.todo.domain.model.TodoStats
import com.example.todo.domain.model.User
import com.example.todo.domain.model.MarsPhoto

fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    userId = userId,
    title = title,
    isCompleted = isCompleted,
    tagIds = tagIds
)

fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    userId = userId,
    title = title,
    isCompleted = isCompleted,
    tagIds = tagIds
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username
)

fun TagDto.toDomain(): Tag = Tag(
    id = id,
    name = name,
    description = description,
    userId = userId
)

fun TodoStatsDto.toDomain(): TodoStats = TodoStats(
    totalCount = totalCount,
    completedCount = completedCount,
    pendingCount = pendingCount
)

fun ItemStatsDto.toDomain(): ItemStats = ItemStats(
    totalCount = totalCount,
    assignedCount = assignedCount
)

fun MarsPhotoDto.toDomain(): MarsPhoto = 
    MarsPhoto(
        id = id,
        imgSrc = imgSrc
    )
