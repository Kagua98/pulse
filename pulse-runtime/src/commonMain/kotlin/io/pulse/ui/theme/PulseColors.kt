package io.pulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// -------------------------------------------------------------------------------------
// Pulse Theme Architecture
// -------------------------------------------------------------------------------------
//
// All colors used by Pulse UI flow through this file. There are two layers:
//
// 1. PulseColorPalette (data class) -- holds themed color tokens. Each built-in theme
//    (Purple, Ocean, etc.) provides its own palette. The active palette is delivered
//    via `LocalPulseColors` composition local and accessed through the `PulseColors`
//    accessor object.
//
// 2. PulseColors (object) -- a convenience accessor so screens can write
//    `PulseColors.surface` instead of `LocalPulseColors.current.surface`. It also
//    holds *static* colors that are theme-independent (method colors, log-level colors,
//    etc.).
//
// DESIGN SYSTEM INTEGRATION:
//    To hook in an external design system, replace the palette values at the point of
//    theme creation. For example, you could:
//      a) Map your design-system tokens into PulseColorPalette fields inside a custom
//         PulseThemeProvider, OR
//      b) Create an adapter that builds a PulseColorPalette from your design-system's
//         CompositionLocal colors (e.g. MaterialTheme.colorScheme).
//    All Pulse screens read colors exclusively from PulseColors/LocalPulseColors, so
//    swapping the palette is sufficient to re-theme the entire library.
//
// SPACING SCALE:
//    The library uses a consistent 4dp-based spacing scale:
//      2dp  -- tight internal padding (badge padding, icon gaps)
//      4dp  -- compact vertical spacing (between related elements)
//      6dp  -- chip padding, small horizontal gaps between filter chips
//      8dp  -- standard element spacing (section gaps, card internal padding)
//      12dp -- content padding (screen edges, card content insets)
//      14dp -- card internal padding (settings cards)
//      16dp -- large card padding, section separators
//    Any deviation from these values should be avoided. When adding new screens,
//    pick the nearest value from the scale above.
// -------------------------------------------------------------------------------------

enum class PulseTheme(val label: String) {
    Purple("Purple"),
    Ocean("Ocean"),
    Forest("Forest"),
    Sunset("Sunset"),
    Ruby("Ruby"),
    Midnight("Black"),
}

data class PulseColorPalette(
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceDim: Color,
    val divider: Color,
    val searchBackground: Color,
    val success: Color,
    val redirect: Color,
    val clientError: Color,
    val serverError: Color,
)

// --- Palettes ---

internal val purplePalette = PulseColorPalette(
    surface = Color(0xFF1E1E2E),
    surfaceVariant = Color(0xFF2A2A3C),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceDim = Color(0xFF9E9E9E),
    divider = Color(0xFF3A3A4C),
    searchBackground = Color(0xFF2A2A3C),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal val oceanPalette = PulseColorPalette(
    surface = Color(0xFF0D1B2A),
    surfaceVariant = Color(0xFF1B2838),
    onSurface = Color(0xFFD8E8F0),
    onSurfaceDim = Color(0xFF7A9BB0),
    divider = Color(0xFF263848),
    searchBackground = Color(0xFF1B2838),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal val forestPalette = PulseColorPalette(
    surface = Color(0xFF0D1F12),
    surfaceVariant = Color(0xFF1A2E1F),
    onSurface = Color(0xFFD4E8D8),
    onSurfaceDim = Color(0xFF7DA887),
    divider = Color(0xFF273D2D),
    searchBackground = Color(0xFF1A2E1F),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal val sunsetPalette = PulseColorPalette(
    surface = Color(0xFF1F1408),
    surfaceVariant = Color(0xFF2E2010),
    onSurface = Color(0xFFEEE0D0),
    onSurfaceDim = Color(0xFFB09878),
    divider = Color(0xFF3D3018),
    searchBackground = Color(0xFF2E2010),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal val rubyPalette = PulseColorPalette(
    surface = Color(0xFF1F0D0D),
    surfaceVariant = Color(0xFF2E1A1A),
    onSurface = Color(0xFFF0D8D8),
    onSurfaceDim = Color(0xFFB07A7A),
    divider = Color(0xFF3D2626),
    searchBackground = Color(0xFF2E1A1A),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal val midnightPalette = PulseColorPalette(
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceDim = Color(0xFF808080),
    divider = Color(0xFF1E1E1E),
    searchBackground = Color(0xFF121212),
    success = Color(0xFF4CAF50),
    redirect = Color(0xFF2196F3),
    clientError = Color(0xFFFF9800),
    serverError = Color(0xFFF44336),
)

internal fun paletteFor(theme: PulseTheme): PulseColorPalette = when (theme) {
    PulseTheme.Purple -> purplePalette
    PulseTheme.Ocean -> oceanPalette
    PulseTheme.Forest -> forestPalette
    PulseTheme.Sunset -> sunsetPalette
    PulseTheme.Ruby -> rubyPalette
    PulseTheme.Midnight -> midnightPalette
}

val LocalPulseColors = staticCompositionLocalOf { purplePalette }

@Composable
fun PulseThemeProvider(theme: PulseTheme = PulseTheme.Purple, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPulseColors provides paletteFor(theme)) {
        content()
    }
}

// Backward-compatible accessor object -- existing code using PulseColors.surface etc. still works.
internal object PulseColors {
    // --- Themed colors (resolved from the active palette) ---
    val surface: Color @Composable get() = LocalPulseColors.current.surface
    val surfaceVariant: Color @Composable get() = LocalPulseColors.current.surfaceVariant
    val onSurface: Color @Composable get() = LocalPulseColors.current.onSurface
    val onSurfaceDim: Color @Composable get() = LocalPulseColors.current.onSurfaceDim
    val divider: Color @Composable get() = LocalPulseColors.current.divider
    val searchBackground: Color @Composable get() = LocalPulseColors.current.searchBackground
    val success: Color @Composable get() = LocalPulseColors.current.success
    val redirect: Color @Composable get() = LocalPulseColors.current.redirect
    val clientError: Color @Composable get() = LocalPulseColors.current.clientError
    val serverError: Color @Composable get() = LocalPulseColors.current.serverError

    // --- Static colors (theme-independent) ---

    // Status colors
    val failed = Color(0xFF9E9E9E)
    val pending = Color(0xFF78909C)
    val warning = Color(0xFFFFEB3B)

    // Method colors
    val methodGet = Color(0xFF4CAF50)
    val methodPost = Color(0xFF2196F3)
    val methodPut = Color(0xFFFF9800)
    val methodPatch = Color(0xFFA66CE8)
    val methodDelete = Color(0xFFF44336)

    // Accent colors used on the home screen
    val teal = Color(0xFF26A69A)
    val blueGrey = Color(0xFF78909C)

    // Log-level colors
    val logVerbose = Color(0xFF78909C)
    val logDebug = Color(0xFF42A5F5)
    val logInfo = Color(0xFF66BB6A)
    val logWarn = Color(0xFFFFA726)
    val logError = Color(0xFFEF5350)

    // Overlay and badge colors
    val overlayScrim = Color(0xFF000000)
    val badgeText = Color(0xFFFFFFFF)

    @Composable
    fun statusColor(code: Int?): Color = when {
        code == null -> pending
        code in 200..299 -> success
        code in 300..399 -> redirect
        code in 400..499 -> clientError
        code in 500..599 -> serverError
        else -> failed
    }

    fun methodColor(method: String): Color = when (method.uppercase()) {
        "GET" -> methodGet
        "POST" -> methodPost
        "PUT" -> methodPut
        "PATCH" -> methodPatch
        "DELETE" -> methodDelete
        else -> failed
    }
}
