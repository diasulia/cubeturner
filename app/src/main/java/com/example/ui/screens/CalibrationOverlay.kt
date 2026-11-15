package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CalibrationPattern
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val UniformityPalette = listOf(
    "Preto Puro (Vazamento)" to Color(0xFF000000),
    "Branco 100% (Poeira LCD)" to Color(0xFFFFFFFF),
    "Vermelho Óptico" to Color(0xFFFF0000),
    "Verde Óptico" to Color(0xFF00FF00),
    "Azul Óptico" to Color(0xFF0055FF),
    "Cinema Quente 6500K" to Color(0xFFFFECC7)
)

@Composable
fun CalibrationOverlay(
    pattern: CalibrationPattern,
    warmthFilter: Float,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var showHud by remember { mutableStateOf(true) }
    var colorIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("calibration_fullscreen_overlay")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showHud = !showHud
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (pattern) {
                CalibrationPattern.FOCUS_KEYSTONE_GRID -> drawFocusKeystoneGrid()
                CalibrationPattern.CONTRAST_PLUGE -> drawContrastPluge()
                CalibrationPattern.SHARPNESS_BURST -> drawSharpnessBurst()
                CalibrationPattern.COLOR_UNIFORMITY -> {
                    drawRect(color = UniformityPalette[colorIndex].second, size = size)
                }
            }

            if (warmthFilter > 0.01f) {
                drawRect(
                    color = Color(0xFFFF8F00).copy(alpha = warmthFilter * 0.45f),
                    size = size
                )
            }
        }

        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xDD0B101B),
                tonalElevation = 8.dp,
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pattern.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = if (pattern == CalibrationPattern.COLOR_UNIFORMITY) {
                                    "Campo atual: ${UniformityPalette[colorIndex].first}"
                                } else {
                                    pattern.instruction
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pattern == CalibrationPattern.COLOR_UNIFORMITY) {
                            FilledTonalButton(
                                onClick = { colorIndex = (colorIndex + 1) % UniformityPalette.size },
                                modifier = Modifier.testTag("cycle_uniformity_color_button")
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = "Trocar cor")
                                Text("  Trocar Cor")
                            }
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("close_calibration_overlay_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar padrão")
                            Text("  Fechar Padrão")
                        }
                        Text(
                            text = "Toque no fundo para ocultar este painel",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFocusKeystoneGrid() {
    val w = size.width
    val h = size.height
    val cyan = Color(0xFF00E5FF)
    val white = Color.White
    val amber = Color(0xFFFFB300)

    // Outer 100% geometry frame (Keystone edge verification)
    drawRect(
        color = amber,
        topLeft = Offset(2f, 2f),
        size = Size(w - 4f, h - 4f),
        style = Stroke(width = 4f)
    )
    // 5% overscan safe frame
    drawRect(
        color = cyan.copy(alpha = 0.65f),
        topLeft = Offset(w * 0.05f, h * 0.05f),
        size = Size(w * 0.90f, h * 0.90f),
        style = Stroke(width = 2f)
    )

    // 8x8 fine optical alignment grid
    val cols = 8
    val rows = 8
    for (i in 1 until cols) {
        val x = (w / cols) * i
        drawLine(
            color = white.copy(alpha = if (i == cols / 2) 0.85f else 0.28f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = if (i == cols / 2) 2.5f else 1.2f
        )
    }
    for (j in 1 until rows) {
        val y = (h / rows) * j
        drawLine(
            color = white.copy(alpha = if (j == rows / 2) 0.85f else 0.28f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = if (j == rows / 2) 2.5f else 1.2f
        )
    }

    // Center + 4 Corner Optical Focus Targets (crucial for single-LCD Magcubic lenses)
    val baseRadius = min(w, h) * 0.11f
    val targets = listOf(
        Offset(w * 0.5f, h * 0.5f) to baseRadius * 1.35f,
        Offset(w * 0.15f, h * 0.18f) to baseRadius,
        Offset(w * 0.85f, h * 0.18f) to baseRadius,
        Offset(w * 0.15f, h * 0.82f) to baseRadius,
        Offset(w * 0.85f, h * 0.82f) to baseRadius
    )

    targets.forEach { (center, radius) ->
        for (ring in 1..4) {
            drawCircle(
                color = if (ring % 2 == 0) cyan else white,
                radius = radius * (ring / 4f),
                center = center,
                style = Stroke(width = 2f)
            )
        }
        // Radial Siemens focus spokes
        val spokes = 24
        for (s in 0 until spokes) {
            val angle = (2.0 * Math.PI * s) / spokes
            val end = Offset(
                x = center.x + (radius * cos(angle)).toFloat(),
                y = center.y + (radius * sin(angle)).toFloat()
            )
            drawLine(
                color = white.copy(alpha = 0.75f),
                start = center,
                end = end,
                strokeWidth = 1.3f
            )
        }
    }
}

private fun DrawScope.drawContrastPluge() {
    val w = size.width
    val h = size.height
    val steps = 16
    val barWidth = w / steps

    // Top 55%: 16-step full grayscale ramp (0% to 100%)
    for (i in 0 until steps) {
        val lum = i.toFloat() / (steps - 1).toFloat()
        drawRect(
            color = Color(lum, lum, lum),
            topLeft = Offset(i * barWidth, 0f),
            size = Size(barWidth + 1f, h * 0.55f)
        )
    }

    // Middle 22%: Near-Black shadow detail patches (1%, 2%, 4%, 6%, 8%)
    val shadowLevels = listOf(0.01f, 0.025f, 0.045f, 0.065f, 0.09f)
    val patchW = w / shadowLevels.size
    shadowLevels.forEachIndexed { idx, v ->
        drawRect(
            color = Color.Black,
            topLeft = Offset(idx * patchW, h * 0.55f),
            size = Size(patchW, h * 0.22f)
        )
        drawRect(
            color = Color(v, v, v),
            topLeft = Offset(idx * patchW + patchW * 0.15f, h * 0.58f),
            size = Size(patchW * 0.7f, h * 0.16f)
        )
    }

    // Bottom 23%: Near-White highlight clipping patches (90%, 93%, 95%, 97%, 99%)
    val highlightLevels = listOf(0.90f, 0.93f, 0.95f, 0.975f, 0.99f)
    highlightLevels.forEachIndexed { idx, v ->
        drawRect(
            color = Color.White,
            topLeft = Offset(idx * patchW, h * 0.77f),
            size = Size(patchW, h * 0.23f)
        )
        drawRect(
            color = Color(v, v, v),
            topLeft = Offset(idx * patchW + patchW * 0.15f, h * 0.80f),
            size = Size(patchW * 0.7f, h * 0.17f)
        )
    }
}

private fun DrawScope.drawSharpnessBurst() {
    val w = size.width
    val h = size.height
    // Neutral 50% gray background so artificial white sharpening halos immediately stand out
    drawRect(color = Color(0xFF777777), size = size)

    val spacings = listOf(12f, 8f, 5f, 3f)
    val blockW = (w * 0.84f) / spacings.size
    val startX = w * 0.08f

    spacings.forEachIndexed { idx, step ->
        val bx = startX + idx * blockW
        // Vertical burst
        var x = bx + 8f
        while (x < bx + blockW - 12f) {
            drawLine(
                color = Color.Black,
                start = Offset(x, h * 0.12f),
                end = Offset(x, h * 0.46f),
                strokeWidth = (step * 0.45f).coerceAtLeast(1.2f)
            )
            x += step
        }
        // Horizontal burst
        var y = h * 0.54f
        while (y < h * 0.88f) {
            drawLine(
                color = Color.White,
                start = Offset(bx + 8f, y),
                end = Offset(bx + blockW - 12f, y),
                strokeWidth = (step * 0.45f).coerceAtLeast(1.2f)
            )
            y += step
        }
    }

    // Diagonal ringing check cross
    drawLine(Color.Black, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = 3f)
    drawCircle(Color.White, radius = min(w, h) * 0.08f, center = center, style = Stroke(width = 3f))
}
