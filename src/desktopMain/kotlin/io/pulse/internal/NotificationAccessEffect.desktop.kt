package io.pulse.internal

import androidx.compose.runtime.Composable

@Composable
internal actual fun NotificationAccessEffect(
    active: Boolean,
    onOpenRequested: () -> Unit,
) {
    // No persistent notification on desktop
}
