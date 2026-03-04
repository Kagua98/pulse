package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

internal actual class ShareContext

@Composable
internal actual fun rememberShareContext(): ShareContext {
    return remember { ShareContext() }
}

internal actual fun shareText(context: ShareContext, title: String, text: String) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {
        // Clipboard not available
    }
}

internal actual fun shareFile(context: ShareContext, title: String, filePath: String, mimeType: String) {
    // File sharing is not supported on desktop
}

internal actual fun writeTextToTempFile(context: ShareContext, text: String): String? = null
