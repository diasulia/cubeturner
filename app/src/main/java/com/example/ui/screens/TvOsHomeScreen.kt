package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputHdmi
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppUpdateChannel
import com.example.data.CalibrationPattern
import com.example.data.CinemaQuickApp
import com.example.data.MagcubicPresetCinemaApps
import com.example.ui.CubeTunerUiState
import com.example.ui.MainDestination
import com.example.ui.theme.SignalEmerald
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class TvHeroShowcase(
    val id: String,
    val eyebrow: String,
    val headline: String,
    val tagline: String,
    val skyTop: Color,
    val horizonGlow: Color,
    val groundDark: Color,
    val accentGlow: Color
)

private fun heroForApp(appId: String): TvHeroShowcase = when (appId) {
    "netflix" -> TvHeroShowcase(
        id = "netflix",
        eyebrow = "Apple TV Style • Cinema",
        headline = "Netflix",
        tagline = "Filmes, séries originais e estreias em tela gigante",
        skyTop = Color(0xFF3A1C08),
        horizonGlow = Color(0xFFD97706),
        groundDark = Color(0xFF0A0605),
        accentGlow = Color(0xFFF59E0B)
    )
    "youtube" -> TvHeroShowcase(
        id = "youtube",
        eyebrow = "Streaming 1080p • Sem Anúncios c/ SmartTube",
        headline = "YouTube",
        tagline = "Criadores, música, shows ao vivo e alta fluidez",
        skyTop = Color(0xFF3B130B),
        horizonGlow = Color(0xFFEA580C),
        groundDark = Color(0xFF0A0707),
        accentGlow = Color(0xFFFF3B30)
    )
    "prime" -> TvHeroShowcase(
        id = "prime",
        eyebrow = "Amazon Originals",
        headline = "Prime Video",
        tagline = "Produções cinematográficas, esportes ao vivo e canais",
        skyTop = Color(0xFF082038),
        horizonGlow = Color(0xFF0284C7),
        groundDark = Color(0xFF050A12),
        accentGlow = Color(0xFF38BDF8)
    )
    "max" -> TvHeroShowcase(
        id = "max",
        eyebrow = "HBO & Warner Bros.",
        headline = "Max",
        tagline = "Grandes clássicos do cinema e séries premiadas",
        skyTop = Color(0xFF1A1040),
        horizonGlow = Color(0xFF6366F1),
        groundDark = Color(0xFF070612),
        accentGlow = Color(0xFF818CF8)
    )
    "livetv" -> TvHeroShowcase(
        id = "livetv",
        eyebrow = "Transmissão Ao Vivo",
        headline = "TV ao Vivo",
        tagline = "Canais abertos, notícias, esportes e programação contínua",
        skyTop = Color(0xFF0B2922),
        horizonGlow = Color(0xFF10B981),
        groundDark = Color(0xFF050B09),
        accentGlow = Color(0xFF34D399)
    )
    "apphub" -> TvHeroShowcase(
        id = "apphub",
        eyebrow = "Sempre Atualizado • Sem Travas",
        headline = "App Store",
        tagline = "Instale e atualize apps livremente via Aptoide TV, Aurora e Play Store",
        skyTop = Color(0xFF072247),
        horizonGlow = Color(0xFF007AFF),
        groundDark = Color(0xFF040914),
        accentGlow = Color(0xFF38BDF8)
    )
    "hdmi" -> TvHeroShowcase(
        id = "hdmi",
        eyebrow = "Sinal Externo Direto",
        headline = "Entrada HDMI",
        tagline = "Conecte notebook, videogame ou TV Box sem latência",
        skyTop = Color(0xFF1E293B),
        horizonGlow = Color(0xFF0EA5E9),
        groundDark = Color(0xFF06090F),
        accentGlow = Color(0xFF38BDF8)
    )
    else -> TvHeroShowcase(
        id = "default",
        eyebrow = "Magic Cube • Cinema",
        headline = "Sala de Projeção",
        tagline = "Navegue pelos aplicativos abaixo com o controle remoto",
        skyTop = Color(0xFF422806),
        horizonGlow = Color(0xFFD97706),
        groundDark = Color(0xFF0C0704),
        accentGlow = Color(0xFFF59E0B)
    )
}

private val DockMainItems: List<CinemaQuickApp> = buildList {
    addAll(MagcubicPresetCinemaApps.streamingDeck)
    add(
        CinemaQuickApp(
            id = "apphub",
            title = "App Store",
            subtitle = "Baixar e atualizar apps",
            packageName = "app.internal.apphub",
            accentHex = 0xFF007AFF,
            badgeText = "APPS"
        )
    )
}

@Composable
fun TvOsHomeScreen(
    uiState: CubeTunerUiState,
    onLaunchPackage: (String, String) -> Unit,
    onCleanLocalRam: () -> Unit,
    onOpenCalibration: (CalibrationPattern) -> Unit,
    onToggleDefaultLauncher: (Boolean) -> Unit,
    onOpenHiddenSettings: (String, String) -> Unit,
    onOpenTechnicalPage: (MainDestination) -> Unit,
    onDismissAppHub: () -> Unit,
    onOpenUpdateChannel: (AppUpdateChannel) -> Unit,
    onCheckPackageUpdate: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAppId by remember { mutableStateOf("netflix") }
    var showControlCenter by remember { mutableStateOf(false) }
    var timeString by remember { mutableStateOf("") }
    val initialFocusRequester = remember { FocusRequester() }

    if (uiState.showAppHubModal) {
        BackHandler(onBack = onDismissAppHub)
    } else if (showControlCenter) {
        BackHandler { showControlCenter = false }
    }

    LaunchedEffect(Unit) {
        val fmt = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
        while (true) {
            timeString = fmt.format(Date())
            delay(15_000)
        }
    }

    LaunchedEffect(Unit) {
        runCatching { initialFocusRequester.requestFocus() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Menu) {
                    showControlCenter = !showControlCenter
                    true
                } else {
                    false
                }
            }
            .testTag("tvos_clean_stage")
    ) {
        val isWide = maxWidth >= 620.dp
        val tileWidth = if (isWide) 132.dp else 118.dp
        val tileHeight = if (isWide) 80.dp else 72.dp

        // 1. Dynamic Apple TV Top Shelf Atmospheric Cinema Backdrop
        Crossfade(
            targetState = heroForApp(activeAppId),
            animationSpec = tween(durationMillis = 420),
            label = "tvosHeroBackdrop"
        ) { showcase ->
            TvOsAtmosphericStage(showcase = showcase)
        }

        // 2. Subtle top-center chevron + Apple TV top-right Control Center time pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(22.dp)
            )

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.28f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .focusable()
                    .clickable { showControlCenter = true }
                    .testTag("tvos_control_center_trigger")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = timeString.ifBlank { "21:00" },
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.94f)
                    )
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Abrir Central de Controle",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // 3. Main Apple TV Stage: SF-proportioned Hero Title -> Frosted Glass Dock -> Secondary Peek Row
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentPadding = PaddingValues(
                start = if (isWide) 34.dp else 18.dp,
                end = if (isWide) 34.dp else 18.dp,
                top = if (isWide) 50.dp else 62.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                val showcase = heroForApp(activeAppId)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isWide) 168.dp else 206.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 520.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = showcase.eyebrow,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                        Text(
                            text = showcase.headline,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = if (isWide) 44.sp else 36.sp,
                                lineHeight = if (isWide) 48.sp else 40.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1.1).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = showcase.tagline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Signature Apple TV Frosted Translucent Glass Dock (with glossy blue App Store icon on right!)
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = Color(0xFFE5DDD5).copy(alpha = 0.21f),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tvos_frosted_glass_dock")
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(
                                if (isWide) 14.dp else 12.dp,
                                Alignment.CenterHorizontally
                            ),
                            contentPadding = PaddingValues(
                                horizontal = if (isWide) 20.dp else 16.dp,
                                vertical = if (isWide) 18.dp else 15.dp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = DockMainItems,
                                key = { it.id }
                            ) { app ->
                                val isFirst = app.id == DockMainItems.first().id
                                TvOsDockTile(
                                    app = app,
                                    tileWidth = tileWidth,
                                    tileHeight = tileHeight,
                                    isHighlighted = activeAppId == app.id,
                                    focusModifier = if (isFirst) {
                                        Modifier.focusRequester(initialFocusRequester)
                                    } else {
                                        Modifier
                                    },
                                    onFocused = { activeAppId = app.id },
                                    onClick = {
                                        activeAppId = app.id
                                        onLaunchPackage(app.packageName, app.title)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Secondary Row peeking softly below the Glass Dock
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        if (isWide) 16.dp else 12.dp,
                        Alignment.CenterHorizontally
                    ),
                    contentPadding = PaddingValues(horizontal = if (isWide) 20.dp else 12.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = MagcubicPresetCinemaApps.utilityDock,
                        key = { it.id }
                    ) { util ->
                        TvOsSecondaryTile(
                            app = util,
                            tileWidth = tileWidth,
                            tileHeight = (tileHeight * 0.86f),
                            onFocused = { activeAppId = util.id },
                            onClick = {
                                activeAppId = util.id
                                onLaunchPackage(util.packageName, util.title)
                            }
                        )
                    }
                }
            }
        }

        // 4. Apple TV App Store & Updates Frosted Modal ("Central de Apps Atualizados")
        AnimatedVisibility(
            visible = uiState.showAppHubModal,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.94f),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.96f),
            modifier = Modifier.fillMaxSize()
        ) {
            TvOsAppHubDialog(
                uiState = uiState,
                onDismiss = onDismissAppHub,
                onOpenChannel = onOpenUpdateChannel,
                onCheckPackageUpdate = onCheckPackageUpdate,
                onOpenNativeAppsSettings = {
                    onDismissAppHub()
                    onOpenHiddenSettings(
                        android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
                        "Aplicativos"
                    )
                }
            )
        }

        // 5. Apple TV Control Center Slide-Over Sheet
        AnimatedVisibility(
            visible = showControlCenter,
            enter = fadeIn(tween(180)) + slideInHorizontally(initialOffsetX = { it / 2 }),
            exit = fadeOut(tween(150)) + slideOutHorizontally(targetOffsetX = { it / 2 }),
            modifier = Modifier.fillMaxSize()
        ) {
            TvOsControlCenterDrawer(
                uiState = uiState,
                timeString = timeString,
                onDismiss = { showControlCenter = false },
                onCleanRam = onCleanLocalRam,
                onOpenAppHub = {
                    showControlCenter = false
                    onLaunchPackage("app.internal.apphub", "App Store")
                },
                onOpenFocusPattern = {
                    showControlCenter = false
                    onOpenCalibration(CalibrationPattern.FOCUS_KEYSTONE_GRID)
                },
                onOpenContrastPattern = {
                    showControlCenter = false
                    onOpenCalibration(CalibrationPattern.CONTRAST_PLUGE)
                },
                onToggleHomeLauncher = onToggleDefaultLauncher,
                onOpenAndroidSettings = {
                    showControlCenter = false
                    onOpenHiddenSettings(android.provider.Settings.ACTION_SETTINGS, "Configurações")
                },
                onOpenTechnicalTools = { dest ->
                    showControlCenter = false
                    onOpenTechnicalPage(dest)
                }
            )
        }
    }
}

@Composable
private fun TvOsAppHubDialog(
    uiState: CubeTunerUiState,
    onDismiss: () -> Unit,
    onOpenChannel: (AppUpdateChannel) -> Unit,
    onCheckPackageUpdate: (String, String) -> Unit,
    onOpenNativeAppsSettings: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .safeDrawingPadding()
            .padding(18.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xF216181E),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .testTag("tvos_apphub_modal")
        ) {
            LazyColumn(
                contentPadding = PaddingValues(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Store & Atualizações",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Substitui a loja antiga travada de fábrica por lojas que atualizam sempre.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_apphub_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

                item {
                    Text(
                        text = "Lojas Recomendadas para Projetor",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF38BDF8)
                    )
                }

                items(uiState.updateChannels, key = { it.id }) { channel ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    if (channel.installedVersion != null) {
                                        Text(
                                            text = "v${channel.installedVersion}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SignalEmerald
                                        )
                                    }
                                }
                                Text(
                                    text = channel.roleDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.68f)
                                )
                            }

                            Button(
                                onClick = { onOpenChannel(channel) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (channel.installedVersion != null) {
                                        SignalEmerald
                                    } else {
                                        Color(0xFF007AFF)
                                    },
                                    contentColor = Color.White
                                ),
                                shape = CircleShape,
                                modifier = Modifier.testTag("apphub_channel_${channel.id}")
                            ) {
                                Icon(
                                    imageVector = if (channel.installedVersion != null) {
                                        Icons.Default.PlayArrow
                                    } else {
                                        Icons.Default.OpenInNew
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (channel.installedVersion != null) "Abrir" else "Obter")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verificar Atualização dos Seus Apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                items(MagcubicPresetCinemaApps.streamingDeck, key = { it.id }) { app ->
                    val ver = uiState.installedStreamingVersions[app.packageName]
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = if (ver != null) "Instalado: v$ver" else app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.58f)
                                )
                            }
                            FilledTonalButton(
                                onClick = { onCheckPackageUpdate(app.packageName, app.title) },
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdateAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Atualizar")
                            }
                        }
                    }
                }

                item {
                    FilledTonalButton(
                        onClick = onOpenNativeAppsSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desinstalar versão antiga de fábrica (caso dê conflito de assinatura)")
                    }
                }
            }
        }
    }
}

@Composable
private fun TvOsAtmosphericStage(showcase: TvHeroShowcase) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                0.0f to showcase.skyTop,
                0.46f to showcase.horizonGlow,
                0.76f to showcase.groundDark,
                1.0f to Color(0xFF040507)
            ),
            size = size
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.26f),
                    showcase.accentGlow.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(w * 0.68f, h * 0.34f),
                radius = w * 0.52f
            ),
            center = Offset(w * 0.68f, h * 0.34f),
            radius = w * 0.52f
        )

        val ridgePath = Path().apply {
            moveTo(0f, h * 0.62f)
            cubicTo(w * 0.22f, h * 0.55f, w * 0.44f, h * 0.60f, w * 0.66f, h * 0.52f)
            cubicTo(w * 0.82f, h * 0.47f, w * 0.92f, h * 0.54f, w, h * 0.51f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = ridgePath,
            brush = Brush.verticalGradient(
                colors = listOf(showcase.groundDark.copy(alpha = 0.88f), Color(0xFF040507)),
                startY = h * 0.48f,
                endY = h
            )
        )

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.46f), Color.Transparent),
                startX = 0f,
                endX = w * 0.6f
            ),
            size = size
        )
    }
}

@Composable
private fun TvOsDockTile(
    app: CinemaQuickApp,
    tileWidth: Dp,
    tileHeight: Dp,
    isHighlighted: Boolean,
    focusModifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var isDpadFocused by remember { mutableStateOf(false) }
    val active = isDpadFocused || isHighlighted

    val scale by animateFloatAsState(
        targetValue = if (active) 1.09f else 1.0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "tvosTileScale"
    )
    val liftY by animateDpAsState(
        targetValue = if (active) (-4).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tvosTileLift"
    )

    val tileBrush = remember(app.id) { tvOsTileGradient(app.id) }
    val shape = RoundedCornerShape(18.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = focusModifier
            .offset(y = liftY)
            .scale(scale)
            .size(width = tileWidth, height = tileHeight)
            .shadow(
                elevation = if (active) 18.dp else 4.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(shape)
            .background(tileBrush)
            .border(
                width = if (active) 2.dp else 0.8.dp,
                color = if (active) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.16f),
                shape = shape
            )
            .onFocusChanged { state ->
                isDpadFocused = state.isFocused
                if (state.isFocused) onFocused()
            }
            .focusable()
            .clickable(onClick = onClick)
            .testTag("tvos_dock_tile_${app.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (active) 0.22f else 0.10f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 90f
                    )
                )
        )

        TvOsTileArtwork(appId = app.id, label = app.title)
    }
}

@Composable
private fun TvOsTileArtwork(appId: String, label: String) {
    when (appId) {
        "netflix" -> {
            Text(
                text = "NETFLIX",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    fontSize = 16.sp
                ),
                color = Color(0xFFE50914)
            )
        }
        "youtube" -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 30.dp, height = 22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = label,
                        tint = Color(0xFFFF2D20),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "YouTube",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = Color.White
                )
            }
        }
        "prime" -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "prime",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = Color.White
                )
                Canvas(modifier = Modifier.size(width = 44.dp, height = 9.dp)) {
                    val smile = Path().apply {
                        moveTo(2f, 2f)
                        quadraticTo(size.width * 0.5f, size.height + 3f, size.width - 2f, 2f)
                    }
                    drawPath(
                        path = smile,
                        color = Color.White,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        "max" -> {
            Text(
                text = "max",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.0).sp
                ),
                color = Color.White
            )
        }
        "livetv" -> {
            Canvas(modifier = Modifier.size(42.dp)) {
                drawConcentricRingsEmblem()
            }
        }
        "apphub" -> {
            // Iconic minimalist crossed-beam "A" App Store emblem matching Attachment 1's rightmost blue tile!
            Canvas(modifier = Modifier.size(38.dp)) {
                drawAppStoreAEmblem()
            }
        }
        else -> {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

private fun DrawScope.drawConcentricRingsEmblem() {
    val c = Offset(size.width / 2f, size.height / 2f)
    drawCircle(Color(0xFFFA114F), size.minDimension * 0.46f, c, style = Stroke(width = 5.2f))
    drawCircle(Color(0xFF92E82A), size.minDimension * 0.31f, c, style = Stroke(width = 5.2f))
    drawCircle(Color(0xFF00D9FF), size.minDimension * 0.16f, c, style = Stroke(width = 5.2f))
}

private fun DrawScope.drawAppStoreAEmblem() {
    val w = size.width
    val h = size.height
    val stroke = 5.2f
    // Left diagonal beam
    drawLine(
        color = Color.White,
        start = Offset(w * 0.54f, h * 0.10f),
        end = Offset(w * 0.18f, h * 0.86f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    // Right diagonal beam
    drawLine(
        color = Color.White,
        start = Offset(w * 0.46f, h * 0.10f),
        end = Offset(w * 0.82f, h * 0.86f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    // Horizontal crossbeam
    drawLine(
        color = Color.White,
        start = Offset(w * 0.14f, h * 0.64f),
        end = Offset(w * 0.86f, h * 0.64f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

private fun tvOsTileGradient(appId: String): Brush = when (appId) {
    "netflix" -> Brush.verticalGradient(listOf(Color(0xFF262629), Color(0xFF0D0D0F)))
    "youtube" -> Brush.verticalGradient(listOf(Color(0xFFFF5E4D), Color(0xFFE01E26)))
    "livetv" -> Brush.verticalGradient(listOf(Color(0xFF242426), Color(0xFF121214)))
    "max" -> Brush.verticalGradient(listOf(Color(0xFFFF4F79), Color(0xFFD81B60)))
    "prime" -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    "apphub" -> Brush.verticalGradient(listOf(Color(0xFF2EB2FF), Color(0xFF0066D6)))
    else -> Brush.verticalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
}

@Composable
private fun TvOsSecondaryTile(
    app: CinemaQuickApp,
    tileWidth: Dp,
    tileHeight: Dp,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.07f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "secScale"
    )
    val accent = Color(app.accentHex)
    val shape = RoundedCornerShape(16.dp)

    val iconVec: ImageVector = when (app.id) {
        "hdmi" -> Icons.Default.SettingsInputHdmi
        "keystone" -> Icons.Default.Tune
        "miracast" -> Icons.Default.Tv
        "usb" -> Icons.Default.Usb
        "apphub" -> Icons.Default.Apps
        else -> Icons.Default.Wifi
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(tileWidth)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .size(width = tileWidth, height = tileHeight)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.48f))
                    )
                )
                .border(
                    width = if (focused) 2.dp else 0.8.dp,
                    color = if (focused) Color.White else Color.White.copy(alpha = 0.20f),
                    shape = shape
                )
                .onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused) onFocused()
                }
                .focusable()
                .clickable(onClick = onClick)
                .testTag("tvos_secondary_tile_${app.id}")
        ) {
            Icon(
                imageVector = iconVec,
                contentDescription = app.title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = app.title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = if (focused) 0.98f else 0.68f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TvOsControlCenterDrawer(
    uiState: CubeTunerUiState,
    timeString: String,
    onDismiss: () -> Unit,
    onCleanRam: () -> Unit,
    onOpenAppHub: () -> Unit,
    onOpenFocusPattern: () -> Unit,
    onOpenContrastPattern: () -> Unit,
    onToggleHomeLauncher: (Boolean) -> Unit,
    onOpenAndroidSettings: () -> Unit,
    onOpenTechnicalTools: (MainDestination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xEE181A20),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .safeDrawingPadding()
                .padding(16.dp)
                .width(330.dp)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .testTag("tvos_control_center_drawer")
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = timeString.ifBlank { "21:00" },
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Central de Controle",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

                item {
                    ControlCenterTile(
                        icon = Icons.Default.SystemUpdateAlt,
                        title = "App Store & Atualizações",
                        subtitle = "Baixar e atualizar apps sem bloqueio",
                        onClick = onOpenAppHub,
                        tag = "cc_open_apphub"
                    )
                }

                item {
                    ControlCenterTile(
                        icon = Icons.Default.CleaningServices,
                        title = "Turbo Memória RAM",
                        subtitle = if (uiState.localTotalRamMb > 0) {
                            "${uiState.localAvailableRamMb} MB livres de ${uiState.localTotalRamMb} MB"
                        } else {
                            "Encerrar apps em segundo plano"
                        },
                        onClick = onCleanRam,
                        tag = "cc_clean_ram"
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ControlCenterCompactTile(
                            icon = Icons.Default.Tune,
                            title = "Grade Foco",
                            onClick = onOpenFocusPattern,
                            modifier = Modifier.weight(1f),
                            tag = "cc_focus_grid"
                        )
                        ControlCenterCompactTile(
                            icon = Icons.Default.Tv,
                            title = "Contraste",
                            onClick = onOpenContrastPattern,
                            modifier = Modifier.weight(1f),
                            tag = "cc_contrast_pluge"
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                                Column {
                                    Text(
                                        text = "Botão Home Padrão",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Abrir sempre nesta tela",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.isHomeLauncherEnabled,
                                onCheckedChange = onToggleHomeLauncher
                            )
                        }
                    }
                }

                item {
                    ControlCenterTile(
                        icon = Icons.Default.Settings,
                        title = "Ajustes Nativos Android",
                        subtitle = "Wi-Fi, Bluetooth e Som reais",
                        onClick = onOpenAndroidSettings,
                        tag = "cc_android_settings"
                    )
                }

                item {
                    ControlCenterTile(
                        icon = Icons.Default.Router,
                        title = "Painel Técnico ADB & Controle",
                        subtitle = "Ferramentas avançadas",
                        onClick = { onOpenTechnicalTools(MainDestination.CONNECT) },
                        tag = "cc_technical_tools"
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlCenterTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.09f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .focusable()
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f)
                )
            }
        }
    }
}

@Composable
private fun ControlCenterCompactTile(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.09f),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .focusable()
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}
