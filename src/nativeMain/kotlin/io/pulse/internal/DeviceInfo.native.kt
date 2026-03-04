package io.pulse.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen

@Composable
internal actual fun rememberDeviceInfoSections(): List<InfoSection> = remember {
    val device = UIDevice.currentDevice
    val process = NSProcessInfo.processInfo
    val screen = UIScreen.mainScreen

    listOf(
        InfoSection(
            title = "Device",
            entries = listOf(
                "Name" to device.name,
                "Model" to device.model,
                "System" to device.systemName,
                "System Version" to device.systemVersion,
            ),
        ),
        InfoSection(
            title = "Process",
            entries = listOf(
                "Process Name" to process.processName,
                "Processor Count" to process.processorCount.toString(),
                "Physical Memory" to "${process.physicalMemory / (1024uL * 1024uL)} MB",
                "OS Version" to process.operatingSystemVersionString,
            ),
        ),
        InfoSection(
            title = "Display",
            entries = listOf(
                "Scale" to screen.scale.toString(),
                "Bounds" to "${screen.bounds.size.width} x ${screen.bounds.size.height}",
                "Native Scale" to screen.nativeScale.toString(),
            ),
        ),
    )
}
