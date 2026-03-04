package io.pulse.internal

import androidx.compose.runtime.Composable

internal expect class ShareContext

@Composable
internal expect fun rememberShareContext(): ShareContext

internal expect fun shareText(context: ShareContext, title: String, text: String)

internal expect fun shareFile(context: ShareContext, title: String, filePath: String, mimeType: String)

internal expect fun writeTextToTempFile(context: ShareContext, text: String): String?
