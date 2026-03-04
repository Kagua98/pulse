package io.pulse.internal

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberDeviceInfoSections(): List<InfoSection> {
    val context = LocalContext.current
    return remember {
        buildList {
            add(appSection(context))
            add(deviceSection())
            add(osSection())
            add(memorySection(context))
            add(displaySection(context))
        }
    }
}

private fun appSection(context: Context): InfoSection {
    val pm = context.packageManager
    val pi = pm.getPackageInfo(context.packageName, 0)
    return InfoSection(
        title = "Application",
        entries = listOf(
            "Package" to context.packageName,
            "Version Name" to (pi.versionName ?: "N/A"),
            "Version Code" to pi.longVersionCode.toString(),
            "Target SDK" to context.applicationInfo.targetSdkVersion.toString(),
            "Min SDK" to context.applicationInfo.minSdkVersion.toString(),
            "First Install" to pi.firstInstallTime.toString(),
        ),
    )
}

private fun deviceSection(): InfoSection = InfoSection(
    title = "Device",
    entries = listOf(
        "Manufacturer" to Build.MANUFACTURER,
        "Model" to Build.MODEL,
        "Brand" to Build.BRAND,
        "Product" to Build.PRODUCT,
        "Board" to Build.BOARD,
        "Hardware" to Build.HARDWARE,
        "Supported ABIs" to Build.SUPPORTED_ABIS.joinToString(", "),
    ),
)

private fun osSection(): InfoSection = InfoSection(
    title = "Operating System",
    entries = listOf(
        "Android Version" to Build.VERSION.RELEASE,
        "SDK Level" to Build.VERSION.SDK_INT.toString(),
        "Security Patch" to Build.VERSION.SECURITY_PATCH,
        "Build ID" to Build.DISPLAY,
        "Bootloader" to Build.BOOTLOADER,
    ),
)

private fun memorySection(context: Context): InfoSection {
    val rt = Runtime.getRuntime()
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)

    val heapUsed = rt.totalMemory() - rt.freeMemory()
    val heapMax = rt.maxMemory()
    val nativeHeap = Debug.getNativeHeapAllocatedSize()

    return InfoSection(
        title = "Memory",
        entries = listOf(
            "Heap Used" to formatMb(heapUsed),
            "Heap Max" to formatMb(heapMax),
            "Heap Usage" to "${(heapUsed * 100 / heapMax)}%",
            "Native Heap" to formatMb(nativeHeap),
            "Device Total RAM" to formatMb(mi.totalMem),
            "Device Available RAM" to formatMb(mi.availMem),
            "Low Memory" to mi.lowMemory.toString(),
        ),
    )
}

private fun displaySection(context: Context): InfoSection {
    val dm = context.resources.displayMetrics
    return InfoSection(
        title = "Display",
        entries = listOf(
            "Resolution" to "${dm.widthPixels} x ${dm.heightPixels}",
            "Density" to dm.density.toString(),
            "DPI" to dm.densityDpi.toString(),
            "Density Qualifier" to when {
                dm.densityDpi <= DisplayMetrics.DENSITY_LOW -> "ldpi"
                dm.densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
                dm.densityDpi <= DisplayMetrics.DENSITY_HIGH -> "hdpi"
                dm.densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
                dm.densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
                else -> "xxxhdpi"
            },
        ),
    )
}

private fun formatMb(bytes: Long): String =
    "${(bytes / (1024 * 1024))} MB"
