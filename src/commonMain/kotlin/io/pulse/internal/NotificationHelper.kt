package io.pulse.internal

/**
 * Shows a persistent (ongoing) notification for accessing Pulse.
 * Only functional on Android; no-op on desktop and native targets.
 *
 * @param context On Android this must be an Android [android.content.Context]; ignored on other platforms.
 */
internal expect fun showPersistentNotification(context: Any?)

/**
 * Dismisses the persistent Pulse notification.
 *
 * @param context On Android this must be an Android [android.content.Context]; ignored on other platforms.
 */
internal expect fun dismissPersistentNotification(context: Any?)
