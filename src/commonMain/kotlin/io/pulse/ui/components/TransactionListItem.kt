package io.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.model.HttpTransaction
import io.pulse.ui.theme.PulseColors
import io.pulse.util.formatBytes
import io.pulse.util.formatDuration
import io.pulse.util.formatTimestamp

@Composable
internal fun TransactionListItem(
    transaction: HttpTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PulseColors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
