package io.pulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pulse.PulseCore
import io.pulse.internal.PulseBackHandler
import io.pulse.model.HttpTransaction
import io.pulse.ui.theme.PulseThemeProvider

internal sealed interface PulseDestination {
    data object Home : PulseDestination
    data object Network : PulseDestination
    data class NetworkDetail(val transaction: HttpTransaction) : PulseDestination
    data object Logs : PulseDestination
    data object Crashes : PulseDestination
    data object Leaks : PulseDestination
    data object Device : PulseDestination
    data object Settings : PulseDestination
}

/**
 * Top-level Pulse screen with built-in multi-tool navigation.
 */
@Composable
internal fun PulseScreen(
    initialDestination: PulseDestination? = null,
    onClose: () -> Unit,
) {
    var destination by remember { mutableStateOf(initialDestination ?: PulseDestination.Home) }

    PulseBackHandler {
        when (destination) {
            PulseDestination.Home -> onClose()
            is PulseDestination.NetworkDetail -> destination = PulseDestination.Network
            else -> destination = PulseDestination.Home
        }
    }

    PulseThemeProvider(theme = PulseCore.currentTheme) {
        when (val dest = destination) {
            PulseDestination.Home -> PulseHome(
                onNavigate = { destination = it },
                onClose = onClose,
            )
            PulseDestination.Network -> TransactionListScreen(
                onTransactionClick = { destination = PulseDestination.NetworkDetail(it) },
                onBack = { destination = PulseDestination.Home },
            )
            is PulseDestination.NetworkDetail -> TransactionDetailScreen(
                transaction = dest.transaction,
                onBack = { destination = PulseDestination.Network },
            )
            PulseDestination.Logs -> LogViewerScreen(
                onBack = { destination = PulseDestination.Home },
            )
            PulseDestination.Crashes -> CrashViewerScreen(
                onBack = { destination = PulseDestination.Home },
            )
            PulseDestination.Leaks -> LeakScreen(
                onBack = { destination = PulseDestination.Home },
            )
            PulseDestination.Device -> DeviceInfoScreen(
                onBack = { destination = PulseDestination.Home },
            )
            PulseDestination.Settings -> SettingsScreen(
                onBack = { destination = PulseDestination.Home },
            )
        }
    }
}
