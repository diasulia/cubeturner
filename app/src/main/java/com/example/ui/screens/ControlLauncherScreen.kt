package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.InstalledAppItem
import com.example.ui.CubeTunerUiState
import com.example.ui.theme.TungstenAmber

@Composable
fun ControlLauncherScreen(
    uiState: CubeTunerUiState,
    onSendKeyEvent: (Int, String) -> Unit,
    onOpenHiddenSettings: (String, String) -> Unit,
    onToggleHomeLauncher: (Boolean) -> Unit,
    onLaunchApp: (String) -> Unit,
    onTogglePinApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var modeIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("control_launcher_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = modeIndex == 0,
                        onClick = { modeIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("segment_wifi_remote")
                    ) {
                        Text("Controle Wi-Fi")
                    }
                    SegmentedButton(
                        selected = modeIndex == 1,
                        onClick = { modeIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("segment_projector_launcher")
                    ) {
                        Text("Modo Projetor (Launcher)")
                    }
                }
            }

            if (modeIndex == 0) {
                item {
                    WifiRemotePadCard(onSendKeyEvent = onSendKeyEvent)
                }
            } else {
                item {
                    HiddenSettingsUnlockerCard(
                        isHomeEnabled = uiState.isHomeLauncherEnabled,
                        onToggleHomeLauncher = onToggleHomeLauncher,
                        onOpenHiddenSettings = onOpenHiddenSettings
                    )
                }

                item {
                    Text(
                        text = "Aplicativos Instalados (${uiState.installedApps.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (uiState.installedApps.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhum app secundário encontrado neste perfil.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.installedApps, key = { it.packageName }) { app ->
                        ProjectorAppRowCard(
                            app = app,
                            onLaunch = { onLaunchApp(app.packageName) },
                            onTogglePin = { onTogglePinApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiRemotePadCard(
    onSendKeyEvent: (Int, String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    fun tapKey(code: Int, name: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSendKeyEvent(code, name)
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Controle Remoto ADB sem Infravermelho",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Não precisa apontar para o sensor traseiro do projetor: os comandos viajam instantaneamente pelo Wi-Fi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Circular D-Pad
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_DPAD_UP, "Cima") },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .size(56.dp)
                        .testTag("remote_dpad_up")
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Cima")
                }

                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_DPAD_DOWN, "Baixo") },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .size(56.dp)
                        .testTag("remote_dpad_down")
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Baixo")
                }

                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_DPAD_LEFT, "Esquerda") },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .size(56.dp)
                        .testTag("remote_dpad_left")
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Esquerda")
                }

                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_DPAD_RIGHT, "Direita") },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(56.dp)
                        .testTag("remote_dpad_right")
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Direita")
                }

                Button(
                    onClick = { tapKey(KeyEvent.KEYCODE_DPAD_CENTER, "OK") },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("remote_dpad_ok")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }

            // Navigation row: Back, Home, Hidden Picture Menu (Key 82)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_BACK, "Voltar") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("remote_btn_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Voltar")
                }

                FilledTonalButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_HOME, "Início") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("remote_btn_home")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Home")
                }

                Button(
                    onClick = { tapKey(KeyEvent.KEYCODE_MENU, "Menu Imagem") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TungstenAmber,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("remote_btn_picture_menu")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Imagem", fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Dica: O botão dourado 'Imagem' envia o código MENU (82), que abre os controles ocultos de Brilho, Contraste e Saturação durante vídeos no Magcubic!",
                style = MaterialTheme.typography.labelMedium,
                color = TungstenAmber
            )

            // Volume row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_VOLUME_DOWN, "Volume -") },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("remote_vol_down")
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Diminuir volume")
                }

                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_VOLUME_MUTE, "Mudo") },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("remote_vol_mute")
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Silenciar")
                }

                FilledIconButton(
                    onClick = { tapKey(KeyEvent.KEYCODE_VOLUME_UP, "Volume +") },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("remote_vol_up")
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Aumentar volume")
                }
            }
        }
    }
}

@Composable
private fun HiddenSettingsUnlockerCard(
    isHomeEnabled: Boolean,
    onToggleHomeLauncher: (Boolean) -> Unit,
    onOpenHiddenSettings: (String, String) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Launcher Escuro & Menus Ocultos do Android",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Quando este APK está instalado direto no projetor, use estes atalhos para pular a interface lenta da Magcubic e abrir os ajustes reais do Android 11.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Registrar como Tela Inicial (Home Launcher)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Mantenha desligado no celular; ative apenas dentro do projetor.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isHomeEnabled,
                    onCheckedChange = onToggleHomeLauncher,
                    modifier = Modifier.testTag("toggle_home_launcher_switch")
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = {
                        onOpenHiddenSettings(Settings.ACTION_SETTINGS, "Configurações do Android")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_android_settings_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Android Real")
                }

                FilledTonalButton(
                    onClick = {
                        onOpenHiddenSettings(
                            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
                            "Opções do Desenvolvedor"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_dev_options_btn")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Desenvolvedor")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = {
                        onOpenHiddenSettings(Settings.ACTION_WIFI_SETTINGS, "Configurações Wi-Fi")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_wifi_settings_btn")
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rede Wi-Fi")
                }

                FilledTonalButton(
                    onClick = {
                        onOpenHiddenSettings(
                            Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
                            "Aplicativos"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_manage_apps_btn")
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apps & RAM")
                }
            }
        }
    }
}

@Composable
private fun ProjectorAppRowCard(
    app: InstalledAppItem,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit
) {
    val iconBitmap = remember(app.packageName, app.icon) {
        app.icon?.toImageBitmapOrNull()
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .focusable()
            .clickable(onClick = onLaunch)
            .testTag("launcher_app_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = app.label,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.testTag("pin_app_${app.packageName}")
            ) {
                Icon(
                    imageVector = if (app.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (app.isPinned) "Desafixar ${app.label}" else "Fixar ${app.label} no topo",
                    tint = if (app.isPinned) TungstenAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Drawable.toImageBitmapOrNull(): ImageBitmap? {
    return runCatching {
        val w = intrinsicWidth.coerceIn(48, 192)
        val h = intrinsicHeight.coerceIn(48, 192)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bmp.asImageBitmap()
    }.getOrNull()
}
