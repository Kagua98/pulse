package io.pulse.internal

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

@Composable
internal actual fun ShakeDetectorEffect(onShake: () -> Unit) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)
    var lastShakeTimestamp by remember { mutableLongStateOf(0L) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate acceleration magnitude excluding gravity
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()) -
                    SensorManager.GRAVITY_EARTH

                if (magnitude > SHAKE_THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTimestamp > SHAKE_DEBOUNCE_MS) {
                        lastShakeTimestamp = now
                        currentOnShake()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op
            }
        }

        if (accelerometer != null) {
            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI,
            )
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }
}

/** Acceleration magnitude (m/s^2 above gravity) that counts as a shake. */
private const val SHAKE_THRESHOLD = 8.0

/** Minimum interval between two shake events in milliseconds. */
private const val SHAKE_DEBOUNCE_MS = 1_000L
