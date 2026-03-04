package io.pulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.pulse.internal.InfoSection
import io.pulse.internal.rememberDeviceInfoSections
import io.pulse.ui.components.PulseTopBar
import io.pulse.ui.theme.PulseColors

@Composable
internal fun DeviceInfoScreen(onBack: () -> Unit) {
    val sections = rememberDeviceInfoSections()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PulseColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        PulseTopBar(title = "Device Info", onBack = onBack)
        HorizontalDivider(color = PulseColors.divider, thickness = 0.5.dp)

        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(12.dp),
            ) {
                sections.forEach { section ->
                    SectionCard(section)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionCard(section: InfoSection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PulseColors.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(14.dp),
    ) {
        Text(
            text = section.title,
            color = PulseColors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        section.entries.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    text = label,
                    color = PulseColors.onSurfaceDim,
                    fontSize = 12.sp,
                    modifier = Modifier.width(130.dp),
                )
                Text(
                    text = value,
                    color = PulseColors.onSurface,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
