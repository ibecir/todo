package com.example.todo.model.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            Json.decodeFromString<List<Int>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
