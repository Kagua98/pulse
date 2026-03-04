package io.pulse

/**
 * Defines how the developer can access the Pulse inspector.
 */
enum class PulseAccessMode {
    /** Draggable floating action button overlay (default, works on all platforms). */
    Fab,

    /** Persistent notification (Android only; no-op on other platforms). */
    Notification,

    /** Shake the device to open the inspector (Android only; no-op on other platforms). */
    ShakeGesture,
}
