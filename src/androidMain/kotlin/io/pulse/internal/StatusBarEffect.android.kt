package io.pulse.internal

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun DarkStatusBarEffect() {
    val activity = LocalContext.current as? Activity ?: return

    DisposableEffect(Unit) {
        val window = activity.window
        val decorView = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            val wasLight = ((controller?.systemBarsAppearance ?: 0) and
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS) != 0
            controller?.setSystemBarsAppearance(
                0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            )
            onDispose {
                if (wasLight) {
                    controller?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    )
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val wasLight = decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
            @Suppress("DEPRECATION")
            run {
                decorView.systemUiVisibility = decorView.systemUiVisibility and
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            onDispose {
                if (wasLight) {
                    @Suppress("DEPRECATION")
                    run {
                        decorView.systemUiVisibility = decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            }
        }
    }
}
