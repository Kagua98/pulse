package io.pulse.internal

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import io.pulse.ui.PulseOverlay

internal class PulseInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val app = context?.applicationContext as? Application ?: return false
        PulsePlatform.init(app)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

internal object PulsePlatform {
    lateinit var appContext: Application
        private set

    fun init(app: Application) {
        appContext = app
        // Check debug flag
        val isDebuggable =
            (app.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggable) {
            throw IllegalStateException(
                "Pulse must only be used in debug builds! " +
                    "Use debugImplementation instead of implementation in your build.gradle.",
            )
        }
        // Auto-inject the overlay into every Activity
        app.registerActivityLifecycleCallbacks(PulseActivityCallbacks())
    }
}

private class PulseActivityCallbacks : Application.ActivityLifecycleCallbacks {

    companion object {
        private const val OVERLAY_TAG = "pulse_auto_overlay"
    }

    override fun onActivityResumed(activity: Activity) {
        val decorView = activity.window.decorView as? ViewGroup ?: return
        // Already injected
        if (decorView.findViewWithTag<View>(OVERLAY_TAG) != null) return

        val overlay = ComposeView(activity).apply {
            tag = OVERLAY_TAG
            setContent {
                PulseOverlay()
            }
        }
        activity.addContentView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
