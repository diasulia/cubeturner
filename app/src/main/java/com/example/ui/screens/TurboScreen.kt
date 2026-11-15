package com.example.ui.screens

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BloatwarePackage
import com.example.data.ConnectionState
import com.example.ui.CubeTunerUiState
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.TungstenAmber

@Composable
fun TurboScreen(
    uiState: CubeTunerUiState,
    onApplyOneTapTurbo: () -> Unit,
    onBoostRam: () -> Unit,
    onSetAnimationScale: (Float) -> Unit,
    onToggleBloatware: (String, Boolean) -> Unit,
    onOpenLocalAppSettings: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = uiState.connectionState is ConnectionState.Connected

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("turbo_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OneTapTurboBannerCard(
                    isConnected = isConnected,
                    localAvailRam = uiState.localAvailableRamMb,
                    localTotalRam = uiState.localTotalRamMb,
                    onApplyOneTapTurbo = onApplyOneTapTurbo,
                    onBoostRam = onBoostRam,
                    onOpenLocalApps = {
                        onOpenLocalAppSettings(
                            Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
                            "Gerenciador de Aplicativos"
                        )
                    }
                )
            }

            item {
                AnimationSpeedCard(
                    currentScale = uiState.telemetry.animationScale,
                    onSelectScale = onSetAnimationScale
                )
            }

            item {
                SafetyNoticeCard()
            }

            item {
                Text(
                    text = "Serviços Ocultos & Bloatware (Allwinner H713 / Hotack)",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(uiState.bloatwareList, key = { it.packageName }) { item ->
                BloatwareItemCard(
                    item = item,
                    onToggleDisable = { disable -> onToggleBloatware(item.packageName, disable) }
                )
            }
        }
    }
}

@Composable
private fun OneTapTurboBannerCard(
    isConnected: Boolean,
    localAvailRam: Int,
    localTotalRam: Int,
    onApplyOneTapTurbo: () -> Unit,
    onBoostRam: () -> Unit,
    onOpenLocalApps: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Modo Turbo",
                    tint = TungstenAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Turbo Allwinner H713 (Sem Root)",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Desativa módulos de proxy e telemetria de fábrica em segundo plano, reduz animações para 0.5x e libera RAM para reprodução fluida de vídeo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isConnected && localTotalRam > 0) {
                Text(
                    text = "RAM livre neste aparelho agora: $localAvailRam MB de $localTotalRam MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalEmerald
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApplyOneTapTurbo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("one_tap_turbo_button")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Otimizar 1-Toque", fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onBoostRam,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("boost_ram_button")
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpar RAM")
                }
            }

            FilledTonalButton(
                onClick = onOpenLocalApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_local_apps_manager_button")
            ) {
                Text("Se instalado direto no projetor: Abrir Gerenciador Nativo de Apps")
            }
        }
    }
}

@Composable
private fun AnimationSpeedCard(
    currentScale: Float,
    onSelectScale: (Float) -> Unit
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
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Velocidade de Animação",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Escala de Animação da Interface",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "Reduzir para 0.5x ou 0x elimina o atraso ao navegar com o controle remoto infravermelho.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val options = listOf(
                    0.0f to "0x Instantâneo",
                    0.5f to "0.5x Rápido",
                    1.0f to "1.0x Padrão"
                )
                options.forEach { (scale, label) ->
                    FilterChip(
                        selected = kotlin.math.abs(currentScale - scale) < 0.1f,
                        onClick = { onSelectScale(scale) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("anim_scale_chip_${(scale * 10).toInt()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyNoticeCard() {
    Surface(
        color = TungstenAmber.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, TungstenAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Aviso de Segurança",
                tint = TungstenAmber
            )
            Text(
                text = "Proteção Anti-Bootloop: A lista abaixo exclui propositalmente o launcher original (com.htc.magcubicos). Nunca desative o launcher de fábrica antes de instalar e definir um launcher substituto!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BloatwareItemCard(
    item: BloatwarePackage,
    onToggleDisable: (Boolean) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = item.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.impactBadge,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (item.isDisabled) {
                        Text(
                            text = "• Desativado (RAM livre)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalEmerald
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = item.isDisabled,
                    onCheckedChange = { wantDisabled -> onToggleDisable(wantDisabled) },
                    modifier = Modifier.testTag("bloat_switch_${item.packageName}")
                )
                Text(
                    text = if (item.isDisabled) "Bloqueado" else "Ativo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isDisabled) SignalEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
