package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CalibrationPattern
import com.example.ui.CubeTunerUiState
import com.example.ui.theme.TungstenAmber

@Composable
fun VisualScreen(
    uiState: CubeTunerUiState,
    onSelectPattern: (CalibrationPattern) -> Unit,
    onWarmthChange: (Float) -> Unit,
    onSetDensityDpi: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("visual_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Estúdio de Calibração Óptica",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Abra qualquer padrão em tela cheia no projetor (ou via espelhamento) para alinhar foco mecânico, keystone e contraste real.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(CalibrationPattern.entries, key = { it.name }) { pattern ->
                CalibrationPatternCard(
                    pattern = pattern,
                    onClick = { onSelectPattern(pattern) }
                )
            }

            item {
                DisplayDensityAdbCard(
                    currentDpi = uiState.telemetry.screenDensityDpi,
                    onSetDensityDpi = onSetDensityDpi
                )
            }

            item {
                ColorWarmthCard(
                    warmth = uiState.colorWarmthPreview,
                    onWarmthChange = onWarmthChange
                )
            }
        }
    }
}

@Composable
private fun CalibrationPatternCard(
    pattern: CalibrationPattern,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("pattern_card_${pattern.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PatternThumbnailCanvas(
                pattern = pattern,
                modifier = Modifier
                    .size(width = 88.dp, height = 58.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = pattern.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = pattern.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Abrir ${pattern.title} em tela cheia",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PatternThumbnailCanvas(
    pattern: CalibrationPattern,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRect(Color.Black, size = size)
        when (pattern) {
            CalibrationPattern.FOCUS_KEYSTONE_GRID -> {
                drawRect(Color(0xFFFFB300), style = Stroke(2f))
                drawLine(Color(0xFF00E5FF), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.5f)
                drawLine(Color(0xFF00E5FF), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.5f)
                drawCircle(Color.White, radius = size.height * 0.28f, style = Stroke(1.5f))
            }
            CalibrationPattern.CONTRAST_PLUGE -> {
                val bars = 6
                val bw = size.width / bars
                for (i in 0 until bars) {
                    val v = i / (bars - 1f)
                    drawRect(Color(v, v, v), Offset(i * bw, 0f), Size(bw + 1f, size.height))
                }
            }
            CalibrationPattern.SHARPNESS_BURST -> {
                drawRect(Color(0xFF555555), size = size)
                var x = 8f
                while (x < size.width - 6f) {
                    drawLine(Color.White, Offset(x, 8f), Offset(x, size.height - 8f), 1.5f)
                    x += 6f
                }
            }
            CalibrationPattern.COLOR_UNIFORMITY -> {
                val third = size.width / 3f
                drawRect(Color(0xFFFF1744), Offset(0f, 0f), Size(third, size.height))
                drawRect(Color(0xFF00E676), Offset(third, 0f), Size(third, size.height))
                drawRect(Color(0xFF2979FF), Offset(third * 2f, 0f), Size(third, size.height))
            }
        }
    }
}

@Composable
private fun DisplayDensityAdbCard(
    currentDpi: Int,
    onSetDensityDpi: (Int?) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Escala DPI via ADB",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Escala Visual da Interface (DPI via ADB)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "Se os ícones e menus do projetor parecerem gigantes ou cortados, ajuste a densidade de pixels via Wi-Fi ADB${if (currentDpi > 0) " (atual: $currentDpi DPI)" else ""}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = { onSetDensityDpi(160) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dpi_160_button")
                ) {
                    Text("160 DPI")
                }
                FilledTonalButton(
                    onClick = { onSetDensityDpi(200) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dpi_200_button")
                ) {
                    Text("200 DPI")
                }
                FilledTonalButton(
                    onClick = { onSetDensityDpi(null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dpi_reset_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Padrão")
                }
            }
        }
    }
}

@Composable
private fun ColorWarmthCard(
    warmth: Float,
    onWarmthChange: (Float) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Temperatura de Cor",
                    tint = TungstenAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filtro Âmbar Cinema (Redução de Luz Azul LED)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "O LED nativo do Magcubic tende ao azul-frio. Use este controle para testar aquecimento D65 nos padrões visuais e no modo Launcher.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = warmth,
                onValueChange = onWarmthChange,
                valueRange = 0f..0.45f,
                modifier = Modifier.testTag("warmth_filter_slider")
            )
        }
    }
}
