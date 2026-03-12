package io.pulse.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.PulseAccessMode
import io.pulse.PulseCore
import io.pulse.internal.DarkStatusBarEffect
import io.pulse.internal.NotificationAccessEffect
import io.pulse.internal.ShakeDetectorEffect
import io.pulse.toDestination
import io.pulse.ui.theme.PulseColors
import kotlin.math.roundToInt

/**
 * Static holder for the FAB drag offset so it survives inspector open/close cycles.
 * Using a simple object avoids losing state when [DraggableFab] leaves composition.
 */
private object FabOffsetHolder {
    var offsetX: Float = 0f
    var offsetY: Float = 0f
}

/**
 * Overlay composable that adds a developer-tools access mechanism to open
 * the Pulse inspector.
 *
 * On Android, this is automatically injected into every Activity — no setup needed.
 * For KMP or manual usage, wrap your app content:
 * ```
 * PulseOverlay {
 *     MyAppContent()
 * }
 * ```
 *
 * @param enabled Whether the overlay is active.
 * @param content Your application content (optional on Android where auto-injection handles it).
 */
@Composable
fun PulseOverlay(
    enabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    var showInspector by remember { mutableStateOf(false) }
    var initialDestination by remember { mutableStateOf<PulseDestination?>(null) }
    val transactions by PulseCore.transactions.collectAsState()
    val currentAccessMode = PulseCore.accessMode

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showInspector) {
            DarkStatusBarEffect()
        }

        if (enabled) {
            when (currentAccessMode) {
                PulseAccessMode.Fab -> {
                    if (!showInspector) {
                        DraggableFab(
                            count = transactions.size,
                            offsetX = FabOffsetHolder.offsetX,
                            offsetY = FabOffsetHolder.offsetY,
                            onOffsetChange = { dx, dy ->
                                FabOffsetHolder.offsetX = dx
                                FabOffsetHolder.offsetY = dy
                            },
                            onClick = { showInspector = true },
                        )
                    }
                }

                PulseAccessMode.Notification -> {
                    NotificationAccessEffect(
                        active = !showInspector,
                        onOpenRequested = {
                            initialDestination = PulseCore.notificationContentType.toDestination()
                            showInspector = true
                        },
                    )
                }

                PulseAccessMode.ShakeGesture -> {
                    if (!showInspector) {
                        ShakeDetectorEffect(
                            onShake = { showInspector = true },
                        )
                    }
                }
            }
        }

        // Performance overlay – visible alongside app content, independent of inspector
        if (PulseCore.showPerformanceOverlay) {
            PerformanceOverlay(
                onDismiss = { PulseCore.showPerformanceOverlay = false },
            )
        }

        AnimatedVisibility(
            visible = showInspector,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            PulseScreen(
                initialDestination = initialDestination,
                onClose = {
                    showInspector = false
                    initialDestination = null
                },
            )
        }
    }
}

@Composable
private fun DraggableFab(
    count: Int,
    offsetX: Float,
    offsetY: Float,
    onOffsetChange: (Float, Float) -> Unit,
    onClick: () -> Unit,
) {
    // Local mutable state backed by the hoisted holder values
    var currentOffsetX by remember { mutableFloatStateOf(offsetX) }
    var currentOffsetY by remember { mutableFloatStateOf(offsetY) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentOffsetX += dragAmount.x
                        currentOffsetY += dragAmount.y
                        onOffsetChange(currentOffsetX, currentOffsetY)
                    }
                },
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = PulseColors.surfaceVariant,
                contentColor = PulseColors.onSurface,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.size(56.dp),
            ) {
                PulseIcon(modifier = Modifier.size(28.dp))
            }

            // Badge count
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .background(PulseColors.serverError, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        color = PulseColors.badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * A debug/bug icon drawn via Canvas from an SVG path.
 * Depicts a bug with antennae — representing developer tools inspection.
 */
@Composable
private fun PulseIcon(modifier: Modifier = Modifier) {
    val color = PulseColors.success
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f

        val path = androidx.compose.ui.graphics.Path().apply {
            // Antenna + flag shape (top-right)
            moveTo(10.94f * sx, 13.5f * sy)
            lineTo(9.62f * sx, 14.82f * sy)
            // Left antenna arc approximation
            cubicTo(8.8f * sx, 13.1f * sy, 5.3f * sx, 12.6f * sy, 3.38f * sx, 14.82f * sy)
            lineTo(1.06f * sx, 13.5f * sy)
            lineTo(0f * sx, 14.56f * sy)
            lineTo(1.72f * sx, 16.28f * sy)
            lineTo(1.5f * sx, 16.5f * sy)
            lineTo(1.5f * sx, 18f * sy)
            lineTo(0f * sx, 18f * sy)
            lineTo(0f * sx, 19.5f * sy)
            lineTo(1.5f * sx, 19.5f * sy)
            lineTo(1.5f * sx, 19.58f * sy)
            cubicTo(1.577f * sx, 20.069f * sy, 1.714f * sx, 20.546f * sy, 1.91f * sx, 21f * sy)
            lineTo(0f * sx, 22.94f * sy)
            lineTo(1.06f * sx, 24f * sy)
            lineTo(2.71f * sx, 22.35f * sy)
            cubicTo(3.71f * sx, 23.35f * sy, 4.81f * sx, 24f * sy, 6f * sx, 24f * sy)
            cubicTo(7.19f * sx, 24f * sy, 8.29f * sx, 23.35f * sy, 9.29f * sx, 22.35f * sy)
            lineTo(10.94f * sx, 24f * sy)
            lineTo(12f * sx, 22.94f * sy)
            lineTo(10.09f * sx, 21f * sy)
            cubicTo(10.288f * sx, 20.536f * sy, 10.426f * sx, 20.049f * sy, 10.5f * sx, 19.55f * sy)
            lineTo(10.5f * sx, 19.45f * sy)
            lineTo(12f * sx, 19.45f * sy)
            lineTo(12f * sx, 18f * sy)
            lineTo(10.5f * sx, 18f * sy)
            lineTo(10.5f * sx, 16.5f * sy)
            lineTo(10.28f * sx, 16.28f * sy)
            lineTo(12f * sx, 14.56f * sy)
            lineTo(10.94f * sx, 13.5f * sy)
            close()

            // Bug head (semicircle)
            moveTo(6f * sx, 13.5f * sy)
            cubicTo(7.24f * sx, 13.5f * sy, 8.25f * sx, 14.51f * sy, 8.25f * sx, 15.75f * sy)
            lineTo(3.75f * sx, 15.75f * sy)
            cubicTo(3.75f * sx, 14.51f * sy, 4.76f * sx, 13.5f * sy, 6f * sx, 13.5f * sy)
            close()

            // Bug body
            moveTo(9f * sx, 19.5f * sy)
            cubicTo(9f * sx, 21.16f * sy, 7.66f * sx, 22.5f * sy, 6f * sx, 22.5f * sy)
            cubicTo(4.34f * sx, 22.5f * sy, 3f * sx, 21.16f * sy, 3f * sx, 19.5f * sy)
            lineTo(3f * sx, 17.25f * sy)
            lineTo(9f * sx, 17.25f * sy)
            lineTo(9f * sx, 19.5f * sy)
            close()

            // Play/signal triangle
            moveTo(23.76f * sx, 0.6f * sy)
            lineTo(23.76f * sx, 1.86f * sy)
            lineTo(13.5f * sx, 8.37f * sy)
            lineTo(13.5f * sx, 6.6f * sy)
            lineTo(22f * sx, 1.23f * sy)
            lineTo(9f * sx, 2f * sy)
            lineTo(9f * sx, 11.46f * sy)
            cubicTo(8.51f * sx, 11.18f * sy, 7.99f * sx, 10.97f * sy, 7.5f * sx, 10.74f * sy)
            lineTo(7.5f * sx, 0.63f * sy)
            lineTo(8.64f * sx, 0f * sy)
            lineTo(23.76f * sx, 9.6f * sy)
            close()
        }

        drawPath(path = path, color = color)
    }
}
