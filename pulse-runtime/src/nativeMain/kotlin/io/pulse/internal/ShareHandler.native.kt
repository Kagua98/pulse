package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

internal actual class ShareContext

@Composable
internal actual fun rememberShareContext(): ShareContext {
    return remember { ShareContext() }
}

internal actual fun shareText(context: ShareContext, title: String, text: String) {
    UIPasteboard.generalPasteboard.string = text
}

internal actual fun shareFile(context: ShareContext, title: String, filePath: String, mimeType: String) {
    // File sharing is not supported on native
}

internal actual fun writeTextToTempFile(context: ShareContext, text: String): String? = null
