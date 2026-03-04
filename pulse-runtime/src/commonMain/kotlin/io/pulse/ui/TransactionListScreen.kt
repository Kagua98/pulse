package io.pulse.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.PulseCore
import io.pulse.internal.rememberShareContext
import io.pulse.model.HttpTransaction
import io.pulse.model.TransactionStatus
import io.pulse.ui.components.EmptyState
import io.pulse.ui.components.ExportDialog
import io.pulse.ui.components.MethodBadge
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.components.SearchFilterBar
import io.pulse.ui.components.StatusCodeBadge
import io.pulse.ui.components.StatusFilter
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatBytes
import io.pulse.util.formatDuration
import io.pulse.util.formatTimestamp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TransactionListScreen(
    onTransactionClick: (HttpTransaction) -> Unit,
    onBack: () -> Unit,
) {
    val transactions by PulseCore.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(StatusFilter.All) }
    val selectedIds = remember { mutableStateListOf<String>() }
    var selectionMode by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    val shareContext = rememberShareContext()

    val filteredTransactions by remember(transactions, searchQuery, selectedFilter) {
        derivedStateOf {
            transactions
                .filter { tx -> matchesFilter(tx, selectedFilter) }
                .filter { tx -> matchesSearch(tx, searchQuery) }
        }
    }

    val selectedTransactions by remember(filteredTransactions, selectedIds.size) {
        derivedStateOf {
            filteredTransactions.filter { it.id in selectedIds }
        }
    }

    fun exitSelectionMode() {
        selectionMode = false
        selectedIds.clear()
    }

    fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        if (selectedIds.isEmpty()) {
            selectionMode = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PulseColors.surface)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            if (selectionMode) {
                // Selection mode top bar
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onExport = { showExportDialog = true },
                    onCancel = { exitSelectionMode() },
                )
            } else {
                PulseTopBar(
                    title = "Network",
                    subtitle = "${transactions.size} calls",
                    onBack = onBack,
                    actions = {
                        TextButton(onClick = { PulseCore.clearNetwork() }) {
                            Text("Clear", color = PulseColors.serverError, fontSize = 12.sp)
                        }
                    },
                )
            }

            HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

            // Search and filter
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
            )

            // Transaction count indicator
            if (filteredTransactions.isNotEmpty()) {
                val countText = if (filteredTransactions.size == transactions.size) {
                    "${filteredTransactions.size} transactions"
                } else {
                    "${filteredTransactions.size} of ${transactions.size} transactions"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = countText,
                        color = PulseColors.onSurfaceDim,
                        fontSize = 11.sp,
                    )
                }
            }

            HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

            // Transaction list
            if (filteredTransactions.isEmpty()) {
                if (transactions.isEmpty()) {
                    EmptyState(
                        title = "No network activity yet",
                        subtitle = "HTTP transactions will appear here as your app makes network requests",
                        icon = "NET",
                    )
                } else {
                    EmptyState(
                        title = "No matching transactions",
                        subtitle = "Try adjusting your search query or filter to find what you're looking for",
                        icon = "?",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.id },
                    ) { transaction ->
                        val isSelected = transaction.id in selectedIds

                        SelectableTransactionListItem(
                            transaction = transaction,
                            selectionMode = selectionMode,
                            isSelected = isSelected,
                            onClick = {
                                if (selectionMode) {
                                    toggleSelection(transaction.id)
                                } else {
                                    onTransactionClick(transaction)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedIds.add(transaction.id)
                                }
                            },
                        )
                    }
                }
            }
        }

        // Export dialog overlay
        if (showExportDialog && selectedTransactions.isNotEmpty()) {
            ExportDialog(
                transactions = selectedTransactions,
                shareContext = shareContext,
                onDismiss = {
                    showExportDialog = false
                    exitSelectionMode()
                },
            )
        }
    }
}

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onExport: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseColors.redirect.copy(alpha = 0.1f))
            .border(
                width = 0.5.dp,
                color = PulseColors.redirect.copy(alpha = 0.2f),
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", color = PulseColors.onSurfaceDim, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))

        // Selection count badge
        Box(
            modifier = Modifier
                .background(
                    PulseColors.redirect.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "$selectedCount selected",
                color = PulseColors.redirect,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onExport,
            enabled = selectedCount > 0,
        ) {
            Text(
                "Export",
                color = if (selectedCount > 0) PulseColors.redirect else PulseColors.onSurfaceDim,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableTransactionListItem(
    transaction: HttpTransaction,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        PulseColors.redirect.copy(alpha = 0.06f)
    } else {
        PulseColors.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                SelectionCheckbox(isSelected = isSelected)
            }

            MethodBadge(method = transaction.method)
            StatusCodeBadge(code = transaction.responseCode)

            Text(
                text = transaction.path.ifBlank { "/" },
                color = PulseColors.onSurface,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = formatDuration(transaction.duration),
                color = PulseColors.onSurfaceDim,
                fontSize = 11.sp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = transaction.host,
                color = PulseColors.onSurfaceDim,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (transaction.responseSize > 0) {
                    Text(
                        text = formatBytes(transaction.responseSize),
                        color = PulseColors.onSurfaceDim,
                        fontSize = 11.sp,
                    )
                }

                Text(
                    text = formatTimestamp(transaction.timestamp),
                    color = PulseColors.onSurfaceDim,
                    fontSize = 11.sp,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            color = PulseColors.divider,
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun SelectionCheckbox(isSelected: Boolean) {
    val bgColor = if (isSelected) {
        PulseColors.redirect
    } else {
        PulseColors.divider
    }
    val checkColor = if (isSelected) {
        PulseColors.surface
    } else {
        PulseColors.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Text(
                text = "\u2713",
                color = checkColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun matchesFilter(transaction: HttpTransaction, filter: StatusFilter): Boolean {
    return when (filter) {
        StatusFilter.All -> true
        StatusFilter.Success -> transaction.responseCode in 200..299
        StatusFilter.Redirect -> transaction.responseCode in 300..399
        StatusFilter.ClientError -> transaction.responseCode in 400..499
        StatusFilter.ServerError -> transaction.responseCode in 500..599
        StatusFilter.Failed -> transaction.status == TransactionStatus.Failed
    }
}

private fun matchesSearch(transaction: HttpTransaction, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.lowercase()
    return transaction.url.lowercase().contains(q) ||
        transaction.host.lowercase().contains(q) ||
        transaction.method.lowercase().contains(q) ||
        transaction.path.lowercase().contains(q) ||
        transaction.responseCode?.toString()?.contains(q) == true
}
