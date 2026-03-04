package io.pulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun ChevronLeftIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.85f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = w * 0.14f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
internal fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.15f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.35f, h * 0.85f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = w * 0.14f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
