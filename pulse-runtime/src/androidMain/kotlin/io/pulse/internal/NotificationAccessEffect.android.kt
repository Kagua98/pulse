package io.pulse.internal

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal actual fun NotificationAccessEffect(
    active: Boolean,
    onOpenRequested: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnOpen by rememberUpdatedState(onOpenRequested)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Track whether we have notification permission
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted && active) {
            showPersistentNotification(context)
        }
    }

    LaunchedEffect(active) {
        if (active && !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Dynamic notification content updater
    val updater = remember { NotificationContentUpdater(context) }

    // Show / dismiss the notification based on active state + permission.
    // When active and permitted, start dynamic content updates which replace
    // the initial static notification with real-time data.
    DisposableEffect(active, hasPermission) {
        if (active && hasPermission) {
            showPersistentNotification(context)
            updater.start()
        } else {
            updater.stop()
            dismissPersistentNotification(context)
        }
        onDispose {
            updater.stop()
            dismissPersistentNotification(context)
        }
    }

    // When notification mode is active, detect taps via the intent extra.
    // The notification's PendingIntent brings the app to foreground with
    // "pulse_notification_tap" = true. We check this on ON_RESUME and consume
    // the extra so it doesn't re-trigger when the inspector is closed.
    if (active) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val activity = context as? Activity ?: return@LifecycleEventObserver
                    val intent = activity.intent ?: return@LifecycleEventObserver
                    if (intent.getBooleanExtra("pulse_notification_tap", false)) {
                        // Consume the extra so it doesn't fire again
                        intent.removeExtra("pulse_notification_tap")
                        currentOnOpen()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}
