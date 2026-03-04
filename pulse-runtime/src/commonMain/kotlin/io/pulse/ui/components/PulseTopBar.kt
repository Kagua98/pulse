package io.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.ui.theme.PulseColors

@Composable
internal fun PulseTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseColors.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TextButton(onClick = onBack) {
                ChevronLeftIcon(
                    modifier = Modifier.size(16.dp),
                    color = PulseColors.onSurface,
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            color = PulseColors.onSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        if (subtitle != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = subtitle, color = PulseColors.onSurfaceDim, fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        actions()
    }
}
