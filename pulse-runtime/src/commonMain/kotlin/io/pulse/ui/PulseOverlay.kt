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
 * A wrench/spanner icon drawn via Canvas.
 * Rotated -45° for a natural tool appearance.
 */
@Composable
private fun PulseIcon(modifier: Modifier = Modifier) {
    val color = PulseColors.success
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        rotate(-45f) {
            val cx = w * 0.5f
            val handleW = w * 0.18f
            val headW = w * 0.44f
            val jawW = w * 0.13f
            val halfHandle = handleW / 2f
            val halfHead = headW / 2f

            // Handle
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(cx - halfHandle, h * 0.42f),
                size = androidx.compose.ui.geometry.Size(handleW, h * 0.52f),
                cornerRadius = CornerRadius(halfHandle),
            )

            // Shoulder (connects jaws to handle)
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(cx - halfHead, h * 0.30f),
                size = androidx.compose.ui.geometry.Size(headW, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.04f),
            )

            // Left jaw
            val jawRadius = jawW * 0.35f
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(cx - halfHead, h * 0.08f),
                size = androidx.compose.ui.geometry.Size(jawW, h * 0.28f),
                cornerRadius = CornerRadius(jawRadius),
            )

            // Right jaw
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(cx + halfHead - jawW, h * 0.08f),
                size = androidx.compose.ui.geometry.Size(jawW, h * 0.28f),
                cornerRadius = CornerRadius(jawRadius),
            )
        }
    }
}
