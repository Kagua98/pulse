package io.pulse.internal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
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
import androidx.core.util.Consumer

@Composable
internal actual fun NotificationAccessEffect(
    active: Boolean,
    onOpenRequested: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnOpen by rememberUpdatedState(onOpenRequested)

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

    // Detect notification taps.
    // The notification's PendingIntent brings the app to foreground with
    // "pulse_notification_tap" = true in the intent extras.
    //
    // Two cases:
    // 1. Cold start: the extra is in activity.intent (checked via LaunchedEffect)
    // 2. Warm start: FLAG_ACTIVITY_SINGLE_TOP delivers via onNewIntent(),
    //    which does NOT update activity.intent — we must use
    //    addOnNewIntentListener to catch it.
    if (active) {
        val activity = context as? ComponentActivity

        // Cold start: check the activity's current intent
        LaunchedEffect(Unit) {
            val intent = activity?.intent
            if (intent?.getBooleanExtra("pulse_notification_tap", false) == true) {
                intent.removeExtra("pulse_notification_tap")
                currentOnOpen()
            }
        }

        // Warm start: listen for new intents delivered via onNewIntent()
        if (activity != null) {
            DisposableEffect(activity) {
                val listener = Consumer<Intent> { intent ->
                    if (intent.getBooleanExtra("pulse_notification_tap", false)) {
                        intent.removeExtra("pulse_notification_tap")
                        currentOnOpen()
                    }
                }
                activity.addOnNewIntentListener(listener)
                onDispose {
                    activity.removeOnNewIntentListener(listener)
                }
            }
        }
    }
}
