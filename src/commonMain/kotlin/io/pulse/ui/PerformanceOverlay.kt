package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.rememberPerformanceSnapshots
import io.pulse.ui.theme.PulseColors
import kotlin.math.roundToInt

/**
 * Persistent offset holder so the overlay position survives recompositions.
 */
private object OverlayOffsetHolder {
    var offsetX: Float = 0f
    var offsetY: Float = 0f
}

@Composable
internal fun PerformanceOverlay(onDismiss: () -> Unit) {
    val snapshot by rememberPerformanceSnapshots()

    var currentOffsetX by remember { mutableFloatStateOf(OverlayOffsetHolder.offsetX) }
    var currentOffsetY by remember { mutableFloatStateOf(OverlayOffsetHolder.offsetY) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentOffsetX += dragAmount.x
                        currentOffsetY += dragAmount.y
                        OverlayOffsetHolder.offsetX = currentOffsetX
                        OverlayOffsetHolder.offsetY = currentOffsetY
                    }
                }
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(PulseColors.overlayScrim.copy(alpha = 0.65f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // CPU metric
                MetricCell(
                    label = "CPU",
                    value = "${snapshot.cpuUsagePercent.toInt()}%",
                    progress = snapshot.cpuUsagePercent / 100f,
                    progressColor = metricColor(snapshot.cpuUsagePercent, highThreshold = 80f, midThreshold = 50f),
                )

                // RAM metric
                MetricCell(
                    label = "RAM",
                    value = "${snapshot.memoryUsedMb}/${snapshot.memoryTotalMb} MB",
                    progress = snapshot.memoryUsagePercent / 100f,
                    progressColor = metricColor(snapshot.memoryUsagePercent, highThreshold = 85f, midThreshold = 60f),
                )

                // FPS metric
                val fpsColor = when {
                    snapshot.fps >= 55 -> PulseColors.success
                    snapshot.fps >= 30 -> PulseColors.warning
                    else -> PulseColors.serverError
                }
                Text(
                    text = "${snapshot.fps} FPS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = fpsColor,
                )

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(18.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = PulseColors.onSurfaceDim,
                    ),
                ) {
                    Text(
                        text = "\u00D7",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PulseColors.onSurfaceDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    progress: Float,
    progressColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = PulseColors.onSurfaceDim,
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = PulseColors.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(72.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = progressColor,
            trackColor = PulseColors.divider,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun metricColor(percent: Float, highThreshold: Float, midThreshold: Float): Color =
    when {
        percent >= highThreshold -> PulseColors.serverError
        percent >= midThreshold -> PulseColors.warning
        else -> PulseColors.success
    }
