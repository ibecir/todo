package com.example.todo.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.TodoStats
import com.example.todo.domain.repository.StatsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StatsRepository {

    override suspend fun exportStats(todoStats: TodoStats, itemStats: ItemStats): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = "todo_stats_${System.currentTimeMillis()}.csv"
            val csvData = generateCsv(todoStats, itemStats)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(fileName, csvData)
            } else {
                saveToFileSystem(fileName, csvData)
            }
        }
    }

    private fun generateCsv(todo: TodoStats, item: ItemStats) = buildString {
        appendLine("Category,Metric,Value")
        appendLine("Todos,Total,${todo.totalCount}")
        appendLine("Todos,Completed,${todo.completedCount}")
        appendLine("Todos,Pending,${todo.pendingCount}")
        appendLine("Items,Total,${item.totalCount}")
        appendLine("Items,Assigned,${item.assignedCount}")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveToMediaStore(name: String, data: String): String {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw Exception("Could not create MediaStore entry")

        context.contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray()) }
            ?: throw Exception("Could not open output stream")

        return uri.toString()
    }

    private fun saveToFileSystem(name: String, data: String): String {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        
        val file = File(dir, name).apply { writeText(data) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
    }
}
