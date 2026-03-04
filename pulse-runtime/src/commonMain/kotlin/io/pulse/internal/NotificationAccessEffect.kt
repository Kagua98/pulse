package io.pulse.internal

import androidx.compose.runtime.Composable

/**
 * Composable effect that manages a persistent notification for accessing
 * the Pulse inspector.
 *
 * When [active] is `true`, shows a persistent notification. When the user
 * taps the notification and brings the app to the foreground, [onOpenRequested]
 * is invoked so the overlay can open the inspector.
 *
 * On disposal (or when [active] becomes `false`) the notification is dismissed.
 *
 * Only functional on Android; no-op on desktop and native targets.
 */
@Composable
internal expect fun NotificationAccessEffect(
    active: Boolean,
    onOpenRequested: () -> Unit,
)
