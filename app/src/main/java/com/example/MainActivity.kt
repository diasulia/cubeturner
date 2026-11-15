package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CubeTunerUiState
import com.example.ui.CubeTunerViewModel
import com.example.ui.MainDestination
import com.example.ui.screens.CalibrationOverlay
import com.example.ui.screens.ConnectScreen
import com.example.ui.screens.ControlLauncherScreen
import com.example.ui.screens.TurboScreen
import com.example.ui.screens.TvOsHomeScreen
import com.example.ui.screens.VisualScreen
import com.example.ui.theme.CubeTunerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val vm: CubeTunerViewModel by viewModels {
        CubeTunerViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CubeTunerTheme {
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                CubeTunerApp(uiState = uiState, viewModel = vm)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME) || intent.action == Intent.ACTION_MAIN) {
            vm.onPhysicalHomePressed()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshLocalDeviceStatus()
        vm.refreshAppHubVersions()
    }
}

private data class NavItemSpec(
    val destination: MainDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val TechnicalNavItems = listOf(
    NavItemSpec(
        MainDestination.HOME,
        R.string.nav_home,
        Icons.Filled.Movie,
        Icons.Outlined.Movie,
        "nav_tab_home"
    ),
    NavItemSpec(
        MainDestination.CONNECT,
        R.string.nav_connect,
        Icons.Filled.Router,
        Icons.Outlined.Router,
        "nav_tab_connect"
    ),
    NavItemSpec(
        MainDestination.TURBO,
        R.string.nav_turbo,
        Icons.Filled.Bolt,
        Icons.Outlined.Bolt,
        "nav_tab_turbo"
    ),
    NavItemSpec(
        MainDestination.VISUAL,
        R.string.nav_visual,
        Icons.Filled.Tune,
        Icons.Outlined.Tune,
        "nav_tab_visual"
    ),
    NavItemSpec(
        MainDestination.CONTROL,
        R.string.nav_control,
        Icons.Filled.Gamepad,
        Icons.Outlined.Gamepad,
        "nav_tab_control"
    )
)

@Composable
fun CubeTunerApp(
    uiState: CubeTunerUiState,
    viewModel: CubeTunerViewModel
) {
    // Auto-dismiss subtle status pills after 3.2 seconds
    LaunchedEffect(uiState.statusBannerMessage) {
        if (uiState.statusBannerMessage != null) {
            delay(3_200)
            viewModel.dismissBanner()
        }
    }

    if (uiState.currentTab != MainDestination.HOME && uiState.activeCalibrationPattern == null) {
        BackHandler {
            viewModel.selectTab(MainDestination.HOME)
        }
    }

    val activePattern = uiState.activeCalibrationPattern
    if (activePattern != null) {
        CalibrationOverlay(
            pattern = activePattern,
            warmthFilter = uiState.colorWarmthPreview,
            onDismiss = { viewModel.openCalibrationPattern(null) }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 100% Pure Clean Apple TV Screen — ZERO bottom/side navigation chrome!
        if (uiState.currentTab == MainDestination.HOME) {
            TvOsHomeScreen(
                uiState = uiState,
                onLaunchPackage = viewModel::launchCinemaTarget,
                onCleanLocalRam = viewModel::cleanLocalRamNow,
                onOpenCalibration = viewModel::openCalibrationPattern,
                onToggleDefaultLauncher = viewModel::toggleHomeLauncherMode,
                onOpenHomeSelector = viewModel::openDefaultHomeSelector,
                onOpenHiddenSettings = viewModel::openHiddenAndroidSettings,
                onOpenTechnicalPage = viewModel::selectTab,
                onDismissAppHub = { viewModel.setShowAppHubModal(false) },
                onOpenUpdateChannel = viewModel::openOrGetUpdateChannel,
                onCheckPackageUpdate = viewModel::openStoreDetailForPackage
            )
        } else {
            // Secondary Technical Tools Mode (only opened on demand via Control Center)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 680.dp
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                TechnicalNavItems.forEach { item ->
                                    val selected = uiState.currentTab == item.destination
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(item.destination) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(id = item.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(id = item.labelRes)) },
                                        modifier = Modifier.testTag(item.tag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                TechnicalNavItems.forEach { item ->
                                    val selected = uiState.currentTab == item.destination
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(item.destination) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(id = item.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(id = item.labelRes)) },
                                        modifier = Modifier.testTag(item.tag)
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (uiState.currentTab) {
                                MainDestination.CONNECT -> ConnectScreen(
                                    uiState = uiState,
                                    onIpChange = viewModel::updateIpInput,
                                    onPortChange = viewModel::updatePortInput,
                                    onConnectClick = viewModel::connectToProjector,
                                    onDisconnectClick = viewModel::disconnectProjector,
                                    onRefreshTelemetry = viewModel::refreshProjectorTelemetry,
                                    onCustomCommandChange = viewModel::updateCustomCommand,
                                    onRunCustomCommand = viewModel::runCustomShellCommand
                                )

                                MainDestination.TURBO -> TurboScreen(
                                    uiState = uiState,
                                    onApplyOneTapTurbo = viewModel::applySafeOneTapOptimization,
                                    onBoostRam = viewModel::boostProjectorRam,
                                    onSetAnimationScale = viewModel::setAnimationScale,
                                    onToggleBloatware = viewModel::toggleBloatwarePackage,
                                    onOpenLocalAppSettings = viewModel::openHiddenAndroidSettings
                                )

                                MainDestination.VISUAL -> VisualScreen(
                                    uiState = uiState,
                                    onSelectPattern = viewModel::openCalibrationPattern,
                                    onWarmthChange = viewModel::updateWarmthPreview,
                                    onSetDensityDpi = viewModel::setProjectorScreenDensity
                                )

                                MainDestination.CONTROL -> ControlLauncherScreen(
                                    uiState = uiState,
                                    onSendKeyEvent = viewModel::sendRemoteKeyEvent,
                                    onOpenHiddenSettings = viewModel::openHiddenAndroidSettings,
                                    onToggleHomeLauncher = viewModel::toggleHomeLauncherMode,
                                    onLaunchApp = viewModel::launchInstalledApp,
                                    onTogglePinApp = viewModel::togglePinApp
                                )

                                MainDestination.HOME -> Unit
                            }
                        }
                    }
                }
            }
        }

        if (uiState.colorWarmthPreview > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFF8F00).copy(alpha = uiState.colorWarmthPreview * 0.25f))
            )
        }

        // Whisper-clean Apple TV glass toast pill at top center
        AnimatedVisibility(
            visible = uiState.statusBannerMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Surface(
                color = Color(0xE61C1C1E),
                shape = CircleShape,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .clickable { viewModel.dismissBanner() }
                    .testTag("status_banner")
            ) {
                Text(
                    text = uiState.statusBannerMessage.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}
