@file:Suppress("DEPRECATION")

package io.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.ShareContext
import io.pulse.internal.generateTransactionsPdf
import io.pulse.internal.shareFile
import io.pulse.internal.shareText
import io.pulse.internal.writeTextToTempFile
import io.pulse.model.HttpTransaction
import io.pulse.ui.theme.PulseColors
import io.pulse.util.exportAsSingleText
import io.pulse.util.exportAsText

@Composable
internal fun ExportDialog(
    transactions: List<HttpTransaction>,
    shareContext: ShareContext,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    val exportText = if (transactions.size == 1) {
        exportAsSingleText(transactions.first())
    } else {
        exportAsText(transactions)
    }

    val title = if (transactions.size == 1) {
        "Export Transaction"
    } else {
        "Export ${transactions.size} Transactions"
    }

    // Full-screen scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        // Dialog card
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .background(PulseColors.surfaceVariant)
                .clickable(enabled = false, onClick = {}) // Block clicks through
                .padding(vertical = 16.dp),
        ) {
            // Title
            Text(
                text = title,
                color = PulseColors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            HorizontalDivider(
                color = PulseColors.divider,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            // Share as Text
            ExportOption(
                label = "Share as Text",
                onClick = {
                    shareText(shareContext, "Export", exportText)
                    onDismiss()
                },
            )

            // Export as TXT File
            ExportOption(
                label = "Export as TXT File",
                onClick = {
                    val txtPath = writeTextToTempFile(shareContext, exportText)
                    if (txtPath != null) {
                        shareFile(shareContext, "Export", txtPath, "text/plain")
                    }
                    onDismiss()
                },
            )

            // Export as PDF (only if supported)
            val pdfPath = generateTransactionsPdf(shareContext, transactions)
            if (pdfPath != null) {
                ExportOption(
                    label = "Export as PDF",
                    onClick = {
                        shareFile(shareContext, "Export", pdfPath, "application/pdf")
                        onDismiss()
                    },
                )
            }

            // Copy to Clipboard
            ExportOption(
                label = "Copy to Clipboard",
                onClick = {
                    clipboardManager.setText(AnnotatedString(exportText))
                    onDismiss()
                },
            )

            Spacer(Modifier.height(8.dp))

            // Cancel button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = "Cancel",
                    color = PulseColors.serverError,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun ExportOption(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = PulseColors.redirect,
            fontSize = 14.sp,
        )
    }
}
