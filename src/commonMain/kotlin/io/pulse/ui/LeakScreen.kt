package io.pulse.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.LeakCanaryLauncher
import io.pulse.internal.getRetainedObjectCount
import io.pulse.internal.isLeakDetectionAvailable
import io.pulse.internal.triggerHeapDump
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.theme.PulseColors

@Composable
internal fun LeakScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(title = "Memory Leaks", onBack = onBack)
        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        if (!isLeakDetectionAvailable) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Leak Detection Unavailable",
                        color = PulseColors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "LeakCanary is only available on Android debug builds",
                        color = PulseColors.onSurfaceDim,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Retained objects card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Retained Objects",
                        color = PulseColors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    val count = getRetainedObjectCount()
                    Text(
                        text = "$count",
                        color = if (count > 0) PulseColors.clientError else PulseColors.success,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = if (count == 0) {
                            "No retained objects detected"
                        } else {
                            "$count objects are being watched for leaks"
                        },
                        color = PulseColors.onSurfaceDim,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                // Actions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Actions",
                        color = PulseColors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    ActionButton(
                        label = "Dump Heap",
                        description = "Trigger a heap dump to analyze memory",
                        onClick = { triggerHeapDump() },
                    )

                    LeakCanaryLauncher()
                }

                // Info card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PulseColors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = "About LeakCanary",
                        color = PulseColors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "LeakCanary automatically detects memory leaks in your Android app. " +
                            "Retained objects are objects that should have been garbage collected but are still " +
                            "held in memory. When enough retained objects accumulate, LeakCanary will dump " +
                            "the heap and analyze it.",
                        color = PulseColors.onSurfaceDim,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseColors.surface, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = PulseColors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                color = PulseColors.onSurfaceDim,
                fontSize = 11.sp,
            )
        }
        TextButton(onClick = onClick) {
            Text("Run", color = PulseColors.clientError, fontSize = 12.sp)
        }
    }
}
