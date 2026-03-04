@file:Suppress("DEPRECATION")

package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.replayRequest
import io.pulse.internal.rememberShareContext
import io.pulse.model.HttpTransaction
import io.pulse.ui.components.BodyViewer
import io.pulse.ui.components.ExportDialog
import io.pulse.ui.components.HeadersPanel
import io.pulse.ui.components.MethodBadge
import io.pulse.ui.components.StatusCodeBadge
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatBytes
import io.pulse.util.formatDuration
import io.pulse.util.formatTimestamp
import io.pulse.util.toCurlCommand
import io.pulse.util.toShareText
import kotlinx.coroutines.launch

private enum class DetailTab(val title: String) {
    Overview("Overview"),
    Request("Request"),
    Response("Response"),
}

@Composable
internal fun TransactionDetailScreen(
    transaction: HttpTransaction,
    onBack: () -> Unit,
) {
    val tabs = DetailTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val clipboardManager = LocalClipboardManager.current
    var showExportDialog by remember { mutableStateOf(false) }
    val shareContext = rememberShareContext()
    val coroutineScope = rememberCoroutineScope()
    var isReplaying by remember { mutableStateOf(false) }
    var replayResult by remember { mutableStateOf<HttpTransaction?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PulseColors.surface)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PulseColors.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) {
                    Text("Back", color = PulseColors.onSurfaceDim, fontSize = 13.sp)
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = {
                        if (!isReplaying) {
                            isReplaying = true
                            coroutineScope.launch {
                                try {
                                    val result = replayRequest(transaction)
                                    replayResult = result
                                } finally {
                                    isReplaying = false
                                }
                            }
                        }
                    },
                ) {
                    Text(
                        text = if (isReplaying) "Replaying..." else "Replay",
                        color = if (isReplaying) PulseColors.pending else PulseColors.success,
                        fontSize = 13.sp,
                    )
                }
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(transaction.toCurlCommand()))
                    },
                ) {
                    Text("cURL", color = PulseColors.redirect, fontSize = 13.sp)
                }
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(transaction.toShareText()))
                    },
                ) {
                    Text("Copy", color = PulseColors.redirect, fontSize = 13.sp)
                }
                TextButton(
                    onClick = { showExportDialog = true },
                ) {
                    Text("Export", color = PulseColors.redirect, fontSize = 13.sp)
                }
            }

            // URL summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PulseColors.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MethodBadge(method = transaction.method)
                    StatusCodeBadge(code = transaction.responseCode)
                    Text(
                        text = formatDuration(transaction.duration),
                        color = PulseColors.onSurfaceDim,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = transaction.url,
                    color = PulseColors.onSurface,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }

            HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

            // Tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PulseColors.surface)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tabs.forEach { tab ->
                    TabItem(
                        title = tab.title,
                        selected = tab == tabs[pagerState.currentPage],
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tabs.indexOf(tab))
                            }
                        },
                    )
                }
            }

            HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

            // Swipeable tab content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (tabs[page]) {
                        DetailTab.Overview -> OverviewTab(transaction)
                        DetailTab.Request -> RequestTab(transaction)
                        DetailTab.Response -> ResponseTab(transaction)
                    }
                }
            }
        }

        // Export dialog overlay
        if (showExportDialog) {
            ExportDialog(
                transactions = listOf(transaction),
                shareContext = shareContext,
                onDismiss = { showExportDialog = false },
            )
        }

        // Replay result dialog overlay
        if (replayResult != null) {
            ReplayResultDialog(
                original = transaction,
                replayed = replayResult!!,
                onDismiss = { replayResult = null },
            )
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (selected) {
        PulseColors.onSurface.copy(alpha = 0.12f)
    } else {
        PulseColors.surface
    }
    val textColor = if (selected) {
        PulseColors.onSurface
    } else {
        PulseColors.onSurfaceDim
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun OverviewTab(transaction: HttpTransaction) {
    Column(modifier = Modifier.padding(12.dp)) {
        SectionTitle("General")
        InfoRow("URL", transaction.url)
        InfoRow("Method", transaction.method)
        InfoRow("Scheme", transaction.scheme)
        InfoRow("Host", transaction.host)
        InfoRow("Path", transaction.path)

        Spacer(Modifier.height(16.dp))
        SectionTitle("Response")
        InfoRow("Status", transaction.responseSummary)
        InfoRow("Duration", formatDuration(transaction.duration))
        InfoRow("Time", formatTimestamp(transaction.timestamp))

        Spacer(Modifier.height(16.dp))
        SectionTitle("Size")
        InfoRow("Request", formatBytes(transaction.requestSize))
        InfoRow("Response", formatBytes(transaction.responseSize))
        InfoRow("Total", formatBytes(transaction.requestSize + transaction.responseSize))

        if (transaction.error != null) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("Error")
            Text(
                text = transaction.error,
                color = PulseColors.serverError,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun RequestTab(transaction: HttpTransaction) {
    val authHeader = transaction.requestHeaders.entries
        .firstOrNull { it.key.equals("Authorization", ignoreCase = true) }
        ?.value

    Column {
        CollapsibleSectionTitle(
            text = "Headers",
            itemCount = transaction.requestHeaders.size,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        HeadersPanel(headers = transaction.requestHeaders)

        Spacer(Modifier.height(8.dp))
        CollapsibleSectionTitle(
            text = "Body",
            charCount = transaction.requestBody?.length,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        BodyViewer(
            body = transaction.requestBody,
            contentType = transaction.requestContentType,
            authorizationHeader = authHeader,
        )
    }
}

@Composable
private fun ResponseTab(transaction: HttpTransaction) {
    Column {
        CollapsibleSectionTitle(
            text = "Headers",
            itemCount = transaction.responseHeaders.size,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        HeadersPanel(headers = transaction.responseHeaders)

        Spacer(Modifier.height(8.dp))
        CollapsibleSectionTitle(
            text = "Body",
            charCount = transaction.responseBody?.length,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
        BodyViewer(
            body = transaction.responseBody,
            contentType = transaction.responseContentType,
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = PulseColors.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun CollapsibleSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    itemCount: Int? = null,
    charCount: Int? = null,
) {
    Row(
        modifier = modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = PulseColors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        if (itemCount != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "($itemCount)",
                color = PulseColors.onSurfaceDim,
                fontSize = 12.sp,
            )
        }
        if (charCount != null && charCount > 0) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "(${formatCharCount(charCount)})",
                color = PulseColors.onSurfaceDim,
                fontSize = 12.sp,
            )
        }
    }
}

private fun formatCharCount(count: Int): String = when {
    count < 1000 -> "$count chars"
    count < 1_000_000 -> "${count / 1000}K chars"
    else -> "${count / 1_000_000}M chars"
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = PulseColors.onSurfaceDim,
            fontSize = 12.sp,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = value,
            color = PulseColors.onSurface,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ReplayResultDialog(
    original: HttpTransaction,
    replayed: HttpTransaction,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(PulseColors.surfaceVariant)
                .clickable(enabled = false, onClick = {})
                .padding(16.dp),
        ) {
            Text(
                text = "Replay Result",
                color = PulseColors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(12.dp))

            // Method + URL
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MethodBadge(method = replayed.method)
                if (replayed.responseCode != null) {
                    StatusCodeBadge(code = replayed.responseCode)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = replayed.url,
                color = PulseColors.onSurface,
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Comparison
            InfoRow("Original Status", original.responseSummary)
            InfoRow("Replay Status", replayed.responseSummary)
            InfoRow("Original Duration", formatDuration(original.duration))
            InfoRow("Replay Duration", formatDuration(replayed.duration))

            if (replayed.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Error: ${replayed.error}",
                    color = PulseColors.serverError,
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "The replayed transaction has been recorded in the transaction list.",
                color = PulseColors.onSurfaceDim,
                fontSize = 11.sp,
            )

            Spacer(Modifier.height(12.dp))

            // Close button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Close",
                    color = PulseColors.redirect,
                    fontSize = 14.sp,
                )
            }
        }
    }
}
