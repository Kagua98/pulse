package io.pulse.internal

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

internal actual class ShareContext(val context: Context)

@Composable
internal actual fun rememberShareContext(): ShareContext {
    val context = LocalContext.current
    return remember { ShareContext(context) }
}

internal actual fun shareText(context: ShareContext, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.context.startActivity(chooser)
}

internal actual fun shareFile(context: ShareContext, title: String, filePath: String, mimeType: String) {
    try {
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) return

        val resolver = context.context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+ (no FileProvider needed)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { output ->
                    sourceFile.inputStream().use { input -> input.copyTo(output) }
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, title).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.context.startActivity(chooser)
            }
        } else {
            // Fallback: share as text for older APIs
            shareText(context, title, sourceFile.readText())
        }
    } catch (_: Exception) {
        // Silently fail — file sharing is best-effort
    }
}

internal actual fun writeTextToTempFile(context: ShareContext, text: String): String? {
    return try {
        val exportDir = File(context.context.cacheDir, "pulse_export")
        exportDir.mkdirs()
        val file = File(exportDir, "export_${System.currentTimeMillis()}.txt")
        file.writeText(text)
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
