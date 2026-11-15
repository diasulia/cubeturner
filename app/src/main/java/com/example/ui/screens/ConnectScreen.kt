package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ConnectionState
import com.example.ui.CubeTunerUiState
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.TungstenAmber

@Composable
fun ConnectScreen(
    uiState: CubeTunerUiState,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    onCustomCommandChange: (String) -> Unit,
    onRunCustomCommand: () -> Unit,
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
                .testTag("connect_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroProjectorCard(
                    connectionState = uiState.connectionState,
                    localIp = uiState.localDeviceIp
                )
            }

            item {
                ConnectionFormCard(
                    ipInput = uiState.ipInput,
                    portInput = uiState.portInput,
                    connectionState = uiState.connectionState,
                    isBusy = uiState.isBusy,
                    onIpChange = onIpChange,
                    onPortChange = onPortChange,
                    onConnectClick = onConnectClick,
                    onDisconnectClick = onDisconnectClick
                )
            }

            if (uiState.connectionState is ConnectionState.Connected) {
                item {
                    LiveProjectorTelemetryCard(
                        uiState = uiState,
                        onRefresh = onRefreshTelemetry
                    )
                }
            }

            item {
                StepByStepGuideCard()
            }

            item {
                AdbShellConsoleCard(
                    logs = uiState.terminalLogs,
                    commandInput = uiState.customCommandInput,
                    isConnected = uiState.connectionState is ConnectionState.Connected,
                    onCommandChange = onCustomCommandChange,
                    onRunCommand = onRunCustomCommand
                )
            }
        }
    }
}

@Composable
private fun HeroProjectorCard(
    connectionState: ConnectionState,
    localIp: String
) {
    val (statusLabel, badgeColor) = when (connectionState) {
        is ConnectionState.Connected -> "PROJETOR CONECTADO" to SignalEmerald
        is ConnectionState.Connecting -> "CONECTANDO..." to TungstenAmber
        is ConnectionState.WaitingAuthorization -> "ACEITE NA TELA DO PROJETOR" to TungstenAmber
        is ConnectionState.Error -> "VERIFIQUE O IP / PERMISSÃO" to MaterialTheme.colorScheme.error
        ConnectionState.Disconnected -> "PRONTO PARA CONECTAR" to MaterialTheme.colorScheme.primary
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(204.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_projector_hero),
                contentDescription = stringResource(id = R.string.hero_banner_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33070A0F),
                                Color(0xCC070A0F),
                                Color(0xF5070A0F)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(1.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color(0xAA101622),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "IP deste aparelho",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localIp,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = "CubeTuner • Magcubic Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.connect_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionFormCard(
    ipInput: String,
    portInput: String,
    connectionState: ConnectionState,
    isBusy: Boolean,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = "Conexão ADB TCP",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.connect_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = onIpChange,
                    label = { Text(stringResource(id = R.string.ip_label)) },
                    placeholder = { Text("192.168.1.45") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("projector_ip_input")
                )

                OutlinedTextField(
                    value = portInput,
                    onValueChange = onPortChange,
                    label = { Text(stringResource(id = R.string.port_label)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFamily),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.3f)
                        .testTag("projector_port_input")
                )
            }

            if (connectionState is ConnectionState.WaitingAuthorization) {
                Surface(
                    color = TungstenAmber.copy(alpha = 0.16f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Olhe para a projeção agora! Marque 'Sempre permitir deste computador' e clique em PERMITIR com o controle remoto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TungstenAmber,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (connectionState is ConnectionState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = connectionState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            val isConnected = connectionState is ConnectionState.Connected
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = if (isConnected) onDisconnectClick else onConnectClick,
                    enabled = !isBusy || isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (isConnected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("connect_toggle_button")
                ) {
                    if (isBusy && !isConnected) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Negociando chave ADB...")
                    } else {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) {
                                stringResource(id = R.string.btn_disconnect)
                            } else {
                                stringResource(id = R.string.btn_connect)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveProjectorTelemetryCard(
    uiState: CubeTunerUiState,
    onRefresh: () -> Unit
) {
    val tel = uiState.telemetry
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Telemetria do Projetor",
                        tint = SignalEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${tel.model} • Android ${tel.androidVersion}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Placa/SoC: ${tel.socBoard} • ${tel.screenDensityDpi} DPI • Animação ${tel.animationScale}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_telemetry_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Atualizar telemetria")
                }
            }

            if (tel.totalRamMb > 0) {
                LinearProgressIndicator(
                    progress = { tel.ramUsageFraction },
                    color = if (tel.ramUsageFraction > 0.82f) TungstenAmber else SignalEmerald,
                    trackColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                )
                Text(
                    text = "RAM em uso: ${tel.usedRamMb} MB / ${tel.totalRamMb} MB (${tel.availableRamMb} MB livres)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun StepByStepGuideCard() {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Passo a Passo Rápido (Você já concluiu os passos 1 e 2!)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            GuideStepRow(
                done = true,
                step = "1. Modo Desenvolvedor e Depuração USB ativos no projetor."
            )
            GuideStepRow(
                done = true,
                step = "2. Projetor e celular/computador conectados no mesmo Wi-Fi."
            )
            GuideStepRow(
                done = false,
                step = "3. No projetor, vá em Configurações → Rede/Wi-Fi → clique no nome da sua rede Wi-Fi e anote o Endereço IP (ex: 192.168.1.45)."
            )
            GuideStepRow(
                done = false,
                step = "4. Baixe o APK deste app (menu Exportar do AI Studio) no seu celular Android na mesma rede Wi-Fi, digite o IP acima e toque em Conectar — ou instale o APK via pendrive direto no projetor!"
            )
        }
    }
}

@Composable
private fun GuideStepRow(done: Boolean, step: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (done) SignalEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Text(
            text = step,
            style = MaterialTheme.typography.bodyMedium,
            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdbShellConsoleCard(
    logs: List<String>,
    commandInput: String,
    isConnected: Boolean,
    onCommandChange: (String) -> Unit,
    onRunCommand: () -> Unit
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
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Console ADB",
                    tint = TungstenAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Terminal ADB Direto",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Surface(
                color = Color(0xFF05070B),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    logs.takeLast(8).forEach { line ->
                        Text(
                            text = "> $line",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = onCommandChange,
                    placeholder = { Text("Ex: getprop ro.product.model") },
                    singleLine = true,
                    enabled = isConnected,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_adb_command_input")
                )
                FilledTonalButton(
                    onClick = onRunCommand,
                    enabled = isConnected && commandInput.isNotBlank(),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("run_custom_adb_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Executar comando")
                }
            }
        }
    }
}
