package io.pulse.internal

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.pulse.Pulse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PulseTileService : TileService() {
    override fun onStartListening() {
        qsTile?.let { tile ->
            val active = PulseTileState.isActive
            tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = "Pulse"
            tile.subtitle = if (active) "Performance Monitor ON" else "Performance Monitor"
            tile.updateTile()
        }
    }

    override fun onClick() {
        PulseTileState.toggle()
        // Sync with Compose state so the performance overlay reacts
        Pulse.showPerformanceOverlay = PulseTileState.isActive
        qsTile?.let { tile ->
            val active = PulseTileState.isActive
            tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.subtitle = if (active) "Performance Monitor ON" else "Performance Monitor"
            tile.updateTile()
        }
        // Bring app to front
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
    }
}

internal object PulseTileState {
    private val _isActive = MutableStateFlow(false)
    val isActive: Boolean get() = _isActive.value
    val isActiveFlow: StateFlow<Boolean> = _isActive

    fun toggle() {
        _isActive.value = !_isActive.value
    }

    fun setActive(active: Boolean) {
        _isActive.value = active
    }
}
