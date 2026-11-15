package com.example.ui

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AdbTcpClient
import com.example.data.AppUpdateChannel
import com.example.data.BloatwarePackage
import com.example.data.CalibrationPattern
import com.example.data.ConnectionState
import com.example.data.CubePreferencesRepository
import com.example.data.InstalledAppItem
import com.example.data.MagcubicKnownBloatware
import com.example.data.MagcubicPresetCinemaApps
import com.example.data.ProjectorTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

enum class MainDestination {
    HOME, VISUAL, TURBO, CONTROL, CONNECT
}

data class CubeTunerUiState(
    val currentTab: MainDestination = MainDestination.HOME,
    val ipInput: String = "",
    val portInput: String = "5555",
    val localDeviceIp: String = "Detectando...",
    val localTotalRamMb: Int = 0,
    val localAvailableRamMb: Int = 0,
    val localDeviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val telemetry: ProjectorTelemetry = ProjectorTelemetry(),
    val bloatwareList: List<BloatwarePackage> = MagcubicKnownBloatware.defaultCatalog,
    val isBusy: Boolean = false,
    val statusBannerMessage: String? = null,
    val terminalLogs: List<String> = listOf(
        "Pronto. Digite o IP do projetor na mesma rede Wi-Fi (porta 5555) e toque em Conectar."
    ),
    val customCommandInput: String = "",
    val activeCalibrationPattern: CalibrationPattern? = null,
    val colorWarmthPreview: Float = 0f,
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isHomeLauncherEnabled: Boolean = false,
    val showAppHubModal: Boolean = false,
    val updateChannels: List<AppUpdateChannel> = defaultUpdateChannels(),
    val installedStreamingVersions: Map<String, String> = emptyMap()
)

private fun defaultUpdateChannels(): List<AppUpdateChannel> = listOf(
    AppUpdateChannel(
        id = "aptoide_tv",
        name = "Aptoide TV (Recomendado p/ Controle)",
        roleDescription = "Loja feita 100% para controle remoto de projetor/TV. Traz Netflix, Globoplay, Disney+, Stremio e Max sempre atualizados.",
        primaryPackage = "cm.aptoidetv.pt",
        altPackage = "cm.aptoide.pt.tv",
        officialWebUrl = "https://tv.aptoide.com"
    ),
    AppUpdateChannel(
        id = "google_play",
        name = "Google Play Store Oficial",
        roleDescription = "Abre a Play Store nativa real do Android (ignorando a loja chinesa travada StoreOS da Magcubic).",
        primaryPackage = "com.android.vending",
        officialWebUrl = "https://play.google.com/store/apps"
    ),
    AppUpdateChannel(
        id = "aurora_store",
        name = "Aurora Store (Sem Bloqueio de Aparelho)",
        roleDescription = "Cliente open-source que baixa versões novas direto da Play Store mesmo quando diz 'incompatível com seu projetor'.",
        primaryPackage = "com.aurora.store",
        officialWebUrl = "https://f-droid.org/pt_BR/packages/com.aurora.store/"
    ),
    AppUpdateChannel(
        id = "smarttube",
        name = "SmartTube (YouTube Leve p/ 1GB RAM)",
        roleDescription = "Substituto open-source ultra rápido para YouTube em projetores Android TV, sem engasgos em 1080p.",
        primaryPackage = "com.teamsmart.videomanager.tv",
        altPackage = "org.smarttube.beta",
        officialWebUrl = "https://smarttubeapp.github.io"
    )
)

class CubeTunerViewModel(
    application: Application,
    private val prefsRepository: CubePreferencesRepository,
    private val adbClient: AdbTcpClient = AdbTcpClient()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CubeTunerUiState())
    val uiState: StateFlow<CubeTunerUiState> = _uiState.asStateFlow()

    private var pinnedSet: Set<String> = emptySet()

    init {
        refreshLocalDeviceStatus()
        loadInstalledApps()
        refreshAppHubVersions()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            prefsRepository.prefsFlow.collect { saved ->
                pinnedSet = saved.pinnedPackages
                _uiState.update { current ->
                    val resolvedIp = when {
                        current.ipInput.isNotBlank() -> current.ipInput
                        saved.ipAddress.isNotBlank() -> saved.ipAddress
                        else -> suggestSubnetPrefix(current.localDeviceIp)
                    }
                    current.copy(
                        ipInput = resolvedIp,
                        portInput = saved.port.toString(),
                        installedApps = current.installedApps.map { app ->
                            app.copy(isPinned = saved.pinnedPackages.contains(app.packageName))
                        }.sortedWith(compareByDescending<InstalledAppItem> { it.isPinned }.thenBy { it.label.lowercase() })
                    )
                }
            }
        }
    }

    fun selectTab(tab: MainDestination) {
        _uiState.update { it.copy(currentTab = tab) }
        if (tab == MainDestination.CONTROL) {
            loadInstalledApps()
        }
    }

    fun updateIpInput(value: String) {
        _uiState.update { it.copy(ipInput = value) }
    }

    fun updatePortInput(value: String) {
        _uiState.update { it.copy(portInput = value.filter { ch -> ch.isDigit() }.take(5)) }
    }

    fun updateCustomCommand(value: String) {
        _uiState.update { it.copy(customCommandInput = value) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun openCalibrationPattern(pattern: CalibrationPattern?) {
        _uiState.update { it.copy(activeCalibrationPattern = pattern) }
    }

    fun updateWarmthPreview(value: Float) {
        _uiState.update { it.copy(colorWarmthPreview = value.coerceIn(0f, 0.45f)) }
    }

    fun refreshLocalDeviceStatus() {
        val ctx = getApplication<Application>()
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val totalMb = (memInfo.totalMem / (1024L * 1024L)).toInt()
        val availMb = (memInfo.availMem / (1024L * 1024L)).toInt()
        val localIp = detectLocalIpv4(ctx)
        val aliasEnabled = isProjectorHomeAliasEnabled(ctx)

        _uiState.update { state ->
            state.copy(
                localDeviceIp = localIp,
                localTotalRamMb = totalMb,
                localAvailableRamMb = availMb,
                isHomeLauncherEnabled = aliasEnabled,
                ipInput = if (state.ipInput.isBlank()) suggestSubnetPrefix(localIp) else state.ipInput
            )
        }
    }

    fun connectToProjector() {
        val ip = _uiState.value.ipInput.trim()
        val port = _uiState.value.portInput.toIntOrNull() ?: 5555
        if (ip.isBlank() || ip.endsWith(".")) {
            _uiState.update {
                it.copy(
                    connectionState = ConnectionState.Error(
                        "Complete o endereço IP do projetor (ex: ${ip.ifBlank { "192.168.1." }}45)."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    connectionState = ConnectionState.Connecting,
                    isBusy = true,
                    statusBannerMessage = null
                )
            }
            appendLog("Conectando via TCP ADB a $ip:$port...")
            prefsRepository.saveConnectionTarget(ip, port)

            val keyPair = prefsRepository.getOrCreateAdbKeyPair()
            val result = adbClient.connect(
                host = ip,
                port = port,
                keyPair = keyPair,
                onWaitingForProjectorPrompt = {
                    _uiState.update { state ->
                        state.copy(connectionState = ConnectionState.WaitingAuthorization)
                    }
                    appendLog("Chave RSA enviada! Olhe para a tela do projetor, marque 'Sempre permitir' e clique em PERMITIR.")
                }
            )

            result.fold(
                onSuccess = { banner ->
                    appendLog("Conectado com sucesso: $banner")
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Connected(banner),
                            isBusy = false,
                            statusBannerMessage = "Conectado ao projetor em $ip:$port!"
                        )
                    }
                    refreshProjectorTelemetry()
                },
                onFailure = { err ->
                    val msg = err.message ?: "Falha na conexão ADB."
                    appendLog("ERRO: $msg")
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Error(msg),
                            isBusy = false
                        )
                    }
                }
            )
        }
    }

    fun disconnectProjector() {
        viewModelScope.launch {
            adbClient.disconnect()
            appendLog("Sessão ADB encerrada.")
            _uiState.update {
                it.copy(
                    connectionState = ConnectionState.Disconnected,
                    statusBannerMessage = "Desconectado do projetor."
                )
            }
        }
    }

    fun refreshProjectorTelemetry() {
        if (!adbClient.isConnected) return
        viewModelScope.launch {
            val compoundCmd = buildString {
                append("echo MODEL=$(getprop ro.product.model);")
                append("echo REL=$(getprop ro.build.version.release);")
                append("echo SOC=$(getprop ro.board.platform);")
                append("grep -E 'MemTotal|MemAvailable' /proc/meminfo;")
                append("wm density;")
                append("echo ANIM=$(settings get global window_animation_scale);")
                append("echo PKGS_ALL; pm list packages;")
                append("echo PKGS_DIS; pm list packages -d")
            }

            adbClient.execShell(compoundCmd).onSuccess { raw ->
                parseTelemetryAndPackages(raw)
            }.onFailure { err ->
                appendLog("Falha ao ler telemetria: ${err.message}")
            }
        }
    }

    private fun parseTelemetryAndPackages(raw: String) {
        var model = "Magcubic Projector"
        var rel = "11"
        var soc = "Allwinner"
        var memTotalKb = 0
        var memAvailKb = 0
        var dpi = 160
        var anim = 1.0f

        val allPackages = mutableSetOf<String>()
        val disabledPackages = mutableSetOf<String>()
        var mode = 0 // 1 = all, 2 = disabled

        raw.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed == "PKGS_ALL" -> mode = 1
                trimmed == "PKGS_DIS" -> mode = 2
                mode == 1 && trimmed.startsWith("package:") -> {
                    allPackages.add(trimmed.removePrefix("package:").trim())
                }
                mode == 2 && trimmed.startsWith("package:") -> {
                    disabledPackages.add(trimmed.removePrefix("package:").trim())
                }
                trimmed.startsWith("MODEL=") -> {
                    val v = trimmed.removePrefix("MODEL=").trim()
                    if (v.isNotEmpty()) model = v
                }
                trimmed.startsWith("REL=") -> {
                    val v = trimmed.removePrefix("REL=").trim()
                    if (v.isNotEmpty()) rel = v
                }
                trimmed.startsWith("SOC=") -> {
                    val v = trimmed.removePrefix("SOC=").trim()
                    if (v.isNotEmpty()) soc = v
                }
                trimmed.startsWith("MemTotal:") -> {
                    memTotalKb = trimmed.filter { it.isDigit() }.toIntOrNull() ?: 0
                }
                trimmed.startsWith("MemAvailable:") -> {
                    memAvailKb = trimmed.filter { it.isDigit() }.toIntOrNull() ?: 0
                }
                trimmed.contains("density:") -> {
                    dpi = trimmed.substringAfterLast("density:").filter { it.isDigit() }.toIntOrNull() ?: dpi
                }
                trimmed.startsWith("ANIM=") -> {
                    val v = trimmed.removePrefix("ANIM=").trim()
                    anim = v.toFloatOrNull() ?: 1.0f
                }
            }
        }

        val updatedBloat = MagcubicKnownBloatware.defaultCatalog.map { item ->
            item.copy(
                isPresentOnDevice = allPackages.contains(item.packageName),
                isDisabled = disabledPackages.contains(item.packageName)
            )
        }

        _uiState.update { state ->
            state.copy(
                telemetry = ProjectorTelemetry(
                    model = model,
                    androidVersion = rel,
                    socBoard = soc,
                    totalRamMb = memTotalKb / 1024,
                    availableRamMb = memAvailKb / 1024,
                    screenDensityDpi = dpi,
                    animationScale = anim
                ),
                bloatwareList = updatedBloat
            )
        }
    }

    fun setAnimationScale(scale: Float) {
        executeAdbAction(
            actionLabel = "Ajustando animações para ${scale}x",
            command = "settings put global window_animation_scale $scale && " +
                "settings put global transition_animation_scale $scale && " +
                "settings put global animator_duration_scale $scale"
        ) {
            refreshProjectorTelemetry()
        }
    }

    fun boostProjectorRam() {
        val beforeMb = _uiState.value.telemetry.availableRamMb
        executeAdbAction(
            actionLabel = "Limpando processos em segundo plano e cache",
            command = "am kill-all; pm trim-caches 512M"
        ) {
            refreshProjectorTelemetry()
            val afterMb = _uiState.value.telemetry.availableRamMb
            val diff = (afterMb - beforeMb).coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (diff > 0) {
                        "Memória otimizada! +$diff MB livres no projetor."
                    } else {
                        "Processos ociosos em segundo plano encerrados."
                    }
                )
            }
        }
    }

    fun toggleBloatwarePackage(pkg: String, shouldDisable: Boolean) {
        val subCmd = if (shouldDisable) "disable-user --user 0" else "enable"
        executeAdbAction(
            actionLabel = if (shouldDisable) "Desativando $pkg" else "Reativando $pkg",
            command = "pm $subCmd $pkg"
        ) { output ->
            appendLog(output)
            refreshProjectorTelemetry()
        }
    }

    fun applySafeOneTapOptimization() {
        val pkgs = MagcubicKnownBloatware.defaultCatalog.joinToString("; ") {
            "pm disable-user --user 0 ${it.packageName}"
        }
        val fullCmd = "$pkgs; settings put global window_animation_scale 0.5; " +
            "settings put global transition_animation_scale 0.5; " +
            "settings put global animator_duration_scale 0.5; am kill-all"

        executeAdbAction(
            actionLabel = "Aplicando pacote completo Turbo H713",
            command = fullCmd
        ) {
            refreshProjectorTelemetry()
            _uiState.update {
                it.copy(statusBannerMessage = "Otimização segura aplicada! Bloatware desativado e animações em 0.5x.")
            }
        }
    }

    fun setProjectorScreenDensity(dpi: Int?) {
        val cmd = if (dpi == null) "wm density reset" else "wm density $dpi"
        executeAdbAction(
            actionLabel = if (dpi == null) "Restaurando DPI original" else "Ajustando densidade visual para $dpi DPI",
            command = cmd
        ) {
            refreshProjectorTelemetry()
        }
    }

    fun sendRemoteKeyEvent(keyCode: Int, keyName: String) {
        if (!adbClient.isConnected) {
            _uiState.update {
                it.copy(statusBannerMessage = "Conecte-se primeiro ao IP do projetor na aba 'Conectar' para usar o controle Wi-Fi.")
            }
            return
        }
        viewModelScope.launch {
            adbClient.execShell("input keyevent $keyCode").onSuccess {
                appendLog("Tecla enviada: $keyName ($keyCode)")
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        connectionState = ConnectionState.Error(err.message ?: "Conexão perdida"),
                        statusBannerMessage = err.message
                    )
                }
            }
        }
    }

    fun runCustomShellCommand() {
        val cmd = _uiState.value.customCommandInput.trim()
        if (cmd.isBlank()) return
        _uiState.update { it.copy(customCommandInput = "") }
        executeAdbAction(actionLabel = "$ $cmd", command = cmd) { output ->
            appendLog(output.ifBlank { "(comando concluído sem saída)" })
        }
    }

    private fun executeAdbAction(
        actionLabel: String,
        command: String,
        onComplete: suspend (String) -> Unit = {}
    ) {
        if (!adbClient.isConnected) {
            _uiState.update {
                it.copy(statusBannerMessage = "Conecte ao IP do projetor na aba 'Conectar' para executar esta ação via ADB.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            appendLog(actionLabel)
            val res = adbClient.execShell(command)
            _uiState.update { it.copy(isBusy = false) }
            res.fold(
                onSuccess = { out -> onComplete(out) },
                onFailure = { err ->
                    appendLog("Erro ADB: ${err.message}")
                    _uiState.update {
                        it.copy(
                            connectionState = ConnectionState.Error(err.message ?: "Erro ADB"),
                            statusBannerMessage = err.message
                        )
                    }
                }
            )
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val ctx = getApplication<Application>()
            val pm = ctx.packageManager
            val myPkg = ctx.packageName

            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)

            val resolved = buildList {
                addAll(pm.queryIntentActivities(launcherIntent, 0))
                addAll(pm.queryIntentActivities(leanbackIntent, 0))
            }

            val items = resolved
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    if (pkg == myPkg) return@mapNotNull null
                    pkg to info
                }
                .distinctBy { it.first }
                .map { (pkg, info) ->
                    InstalledAppItem(
                        label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg,
                        packageName = pkg,
                        icon = runCatching { info.loadIcon(pm) }.getOrNull(),
                        isPinned = pinnedSet.contains(pkg)
                    )
                }
                .sortedWith(compareByDescending<InstalledAppItem> { it.isPinned }.thenBy { it.label.lowercase() })

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(installedApps = items) }
            }
        }
    }

    fun launchInstalledApp(packageName: String) {
        launchCinemaTarget(packageName, packageName)
    }

    fun setShowAppHubModal(visible: Boolean) {
        if (visible) {
            refreshAppHubVersions()
        }
        _uiState.update { it.copy(showAppHubModal = visible) }
    }

    fun refreshAppHubVersions() {
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager

        fun versionOf(pkg: String?): String? {
            if (pkg.isNullOrBlank()) return null
            return runCatching {
                pm.getPackageInfo(pkg, 0).versionName ?: "Instalado"
            }.getOrNull()
        }

        val updatedChannels = defaultUpdateChannels().map { ch ->
            ch.copy(
                installedVersion = versionOf(ch.primaryPackage) ?: versionOf(ch.altPackage)
            )
        }

        val streamMap = buildMap {
            MagcubicPresetCinemaApps.streamingDeck.forEach { app ->
                versionOf(app.packageName)?.let { ver -> put(app.packageName, ver) }
            }
        }

        _uiState.update {
            it.copy(
                updateChannels = updatedChannels,
                installedStreamingVersions = streamMap
            )
        }
    }

    fun openOrGetUpdateChannel(channel: AppUpdateChannel) {
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(channel.primaryPackage)
            ?: pm.getLeanbackLaunchIntentForPackage(channel.primaryPackage)
            ?: channel.altPackage?.let {
                pm.getLaunchIntentForPackage(it) ?: pm.getLeanbackLaunchIntentForPackage(it)
            }

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { ctx.startActivity(launchIntent) }
            return
        }

        openWebUrl(channel.officialWebUrl, channel.name)
    }

    fun openStoreDetailForPackage(packageName: String, title: String) {
        val ctx = getApplication<Application>()
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching {
            ctx.startActivity(marketIntent)
        }.onFailure {
            openWebUrl("https://play.google.com/store/apps/details?id=$packageName", title)
        }
    }

    private fun openWebUrl(url: String, label: String) {
        val ctx = getApplication<Application>()
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            ctx.startActivity(webIntent)
        }.onFailure {
            _uiState.update {
                it.copy(statusBannerMessage = "Para baixar $label, acesse $url ou instale via ADB no Windows.")
            }
        }
    }

    fun launchCinemaTarget(packageName: String, friendlyTitle: String) {
        if (packageName == "app.internal.apphub") {
            setShowAppHubModal(true)
            return
        }
        val ctx = getApplication<Application>()
        val pm = ctx.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
            ?: pm.getLeanbackLaunchIntentForPackage(packageName)
            ?: when (packageName) {
                "com.android.tv.settings", "com.htc.htcsettings" -> Intent(Settings.ACTION_SETTINGS)
                else -> null
            }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { ctx.startActivity(intent) }.onFailure {
                _uiState.update {
                    it.copy(statusBannerMessage = "Não foi possível iniciar $friendlyTitle ($packageName).")
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    statusBannerMessage = "Atalho pronto para '$friendlyTitle' ($packageName) — abrirá direto quando instalado no seu Magic Cube!"
                )
            }
        }
    }

    fun cleanLocalRamNow() {
        val ctx = getApplication<Application>()
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val before = _uiState.value.localAvailableRamMb
        _uiState.value.installedApps.forEach { app ->
            runCatching { am?.killBackgroundProcesses(app.packageName) }
        }
        refreshLocalDeviceStatus()
        val after = _uiState.value.localAvailableRamMb
        val gained = (after - before).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                statusBannerMessage = if (gained > 0) {
                    "Memória limpa! +$gained MB liberados (${after} MB livres)."
                } else {
                    "Processos em segundo plano limpos (${after} MB livres)."
                }
            )
        }
    }

    fun togglePinApp(packageName: String) {
        viewModelScope.launch {
            prefsRepository.togglePinnedPackage(packageName)
        }
    }

    fun openHiddenAndroidSettings(action: String, friendlyName: String) {
        val ctx = getApplication<Application>()
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            ctx.startActivity(intent)
        }.onFailure {
            val fallback = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { ctx.startActivity(fallback) }.onFailure {
                _uiState.update {
                    it.copy(statusBannerMessage = "O sistema atual bloqueou a abertura direta de $friendlyName.")
                }
            }
        }
    }

    fun toggleHomeLauncherMode(enable: Boolean) {
        val ctx = getApplication<Application>()
        val alias = ComponentName(ctx, "com.example.ProjectorHomeAlias")
        val newState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        ctx.packageManager.setComponentEnabledSetting(
            alias,
            newState,
            PackageManager.DONT_KILL_APP
        )
        _uiState.update {
            it.copy(
                isHomeLauncherEnabled = enable,
                statusBannerMessage = if (enable) {
                    "Modo Launcher ativado! Pressione o botão Home (Casinha) do controle para escolher o CubeTuner."
                } else {
                    "Modo Launcher padrão desativado neste aparelho."
                }
            )
        }
    }

    private fun isProjectorHomeAliasEnabled(context: Context): Boolean {
        val alias = ComponentName(context, "com.example.ProjectorHomeAlias")
        return context.packageManager.getComponentEnabledSetting(alias) ==
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    private fun appendLog(entry: String) {
        _uiState.update { state ->
            val updated = (state.terminalLogs + entry).takeLast(40)
            state.copy(terminalLogs = updated)
        }
    }

    private fun detectLocalIpv4(context: Context): String {
        runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val active = cm?.activeNetwork
            val linkProps = active?.let { cm.getLinkProperties(it) }
            val addr = linkProps?.linkAddresses
                ?.mapNotNull { it.address as? Inet4Address }
                ?.firstOrNull { !it.isLoopbackAddress }
            if (addr != null) return addr.hostAddress ?: "Sem Wi-Fi"
        }
        runCatching {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in intf.inetAddresses.toList()) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: continue
                    }
                }
            }
        }
        return "Sem rede local"
    }

    private fun suggestSubnetPrefix(localIp: String): String {
        val parts = localIp.split(".")
        if (parts.size == 4 && (parts[0] == "192" || parts[0] == "172" || (parts[0] == "10" && parts[1] != "0"))) {
            return "${parts[0]}.${parts[1]}.${parts[2]}."
        }
        return "192.168.1."
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CubeTunerViewModel(
                        application = application,
                        prefsRepository = CubePreferencesRepository(application)
                    ) as T
                }
            }
    }
}
