package com.example.data

import android.graphics.drawable.Drawable

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object WaitingAuthorization : ConnectionState
    data class Connected(val banner: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class ProjectorTelemetry(
    val model: String = "Magcubic Android",
    val androidVersion: String = "11",
    val socBoard: String = "Allwinner H713",
    val totalRamMb: Int = 0,
    val availableRamMb: Int = 0,
    val screenDensityDpi: Int = 0,
    val animationScale: Float = 0.0f
) {
    val usedRamMb: Int
        get() = (totalRamMb - availableRamMb).coerceAtLeast(0)

    val ramUsageFraction: Float
        get() = if (totalRamMb > 0) (usedRamMb.toFloat() / totalRamMb.toFloat()).coerceIn(0f, 1f) else 0f
}

data class BloatwarePackage(
    val packageName: String,
    val title: String,
    val description: String,
    val impactBadge: String,
    val isPresentOnDevice: Boolean = false,
    val isDisabled: Boolean = false
)

data class CinemaQuickApp(
    val id: String,
    val title: String,
    val subtitle: String,
    val packageName: String,
    val accentHex: Long,
    val badgeText: String
)

enum class CalibrationPattern(
    val title: String,
    val subtitle: String,
    val instruction: String
) {
    FOCUS_KEYSTONE_GRID(
        title = "Grade Óptica & Keystone",
        subtitle = "Alinhamento 16:9 e foco nos 4 cantos",
        instruction = "Gire o anel de foco físico até que os alvos nos 4 cantos e no centro fiquem igualmente nítidos."
    ),
    CONTRAST_PLUGE(
        title = "Contraste & Nível de Preto",
        subtitle = "Escala de 16 tons de cinza e sub-preto",
        instruction = "Aperte a tecla Menu (3 traços) do controle e ajuste Brilho/Contraste até distinguir todas as barras de 2% a 98%."
    ),
    SHARPNESS_BURST(
        title = "Nitidez & Micro-Resolução",
        subtitle = "Pares de linhas de alta frequência",
        instruction = "Reduza a Nitidez artificial se notar halos brancos ao redor das linhas finas e textos."
    ),
    COLOR_UNIFORMITY(
        title = "Pureza de Cores & Painel LCD",
        subtitle = "Inspeção de poeira interna e cores RGB",
        instruction = "Toque no botão 'Trocar Cor' para alternar entre Preto Puro, Branco, Vermelho, Verde, Azul e Âmbar."
    )
}

data class InstalledAppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable?,
    val isPinned: Boolean = false
)

object MagcubicPresetCinemaApps {
    val streamingDeck = listOf(
        CinemaQuickApp(
            id = "netflix",
            title = "Netflix",
            subtitle = "Filmes & Séries",
            packageName = "com.netflix.mediaclient",
            accentHex = 0xFFE50914,
            badgeText = "N"
        ),
        CinemaQuickApp(
            id = "youtube",
            title = "YouTube",
            subtitle = "Vídeos & 1080p",
            packageName = "com.google.android.youtube.tv",
            accentHex = 0xFFFF3B30,
            badgeText = "YT"
        ),
        CinemaQuickApp(
            id = "livetv",
            title = "TV ao Vivo",
            subtitle = "Canais & GTV",
            packageName = "com.mm.droid.livetv.gtv",
            accentHex = 0xFF10B981,
            badgeText = "TV"
        ),
        CinemaQuickApp(
            id = "max",
            title = "Max",
            subtitle = "HBO & Cinema",
            packageName = "com.wbd.stream",
            accentHex = 0xFF6366F1,
            badgeText = "MAX"
        ),
        CinemaQuickApp(
            id = "prime",
            title = "Prime Video",
            subtitle = "Amazon Originals",
            packageName = "com.amazon.amazonvideo.livingroom",
            accentHex = 0xFF00A8E1,
            badgeText = "PV"
        )
    )

    val utilityDock = listOf(
        CinemaQuickApp(
            id = "hdmi",
            title = "Entrada HDMI",
            subtitle = "Notebook / Videogame",
            packageName = "com.softwinner.awsource",
            accentHex = 0xFF00E5FF,
            badgeText = "HDMI"
        ),
        CinemaQuickApp(
            id = "keystone",
            title = "Projeção & Keystone",
            subtitle = "Ângulo e Zoom",
            packageName = "com.htc.htcsettings",
            accentHex = 0xFFFFB300,
            badgeText = "4-PT"
        ),
        CinemaQuickApp(
            id = "miracast",
            title = "Espelhar Celular",
            subtitle = "Miracast sem fio",
            packageName = "com.softwinner.miracastReceiver",
            accentHex = 0xFF38BDF8,
            badgeText = "CAST"
        ),
        CinemaQuickApp(
            id = "usb",
            title = "Pendrive USB",
            subtitle = "Arquivos e Mídias",
            packageName = "com.softwinner.TvdFileManager",
            accentHex = 0xFFA78BFA,
            badgeText = "USB"
        ),
        CinemaQuickApp(
            id = "apphub",
            title = "App Store & Updates",
            subtitle = "Baixar apps atualizados",
            packageName = "app.internal.apphub",
            accentHex = 0xFF007AFF,
            badgeText = "APPS"
        ),
        CinemaQuickApp(
            id = "tvsettings",
            title = "Wi-Fi & Bluetooth",
            subtitle = "Android TV Nativo",
            packageName = "com.android.tv.settings",
            accentHex = 0xFF8E8E93,
            badgeText = "CFG"
        )
    )
}

data class AppUpdateChannel(
    val id: String,
    val name: String,
    val roleDescription: String,
    val primaryPackage: String,
    val altPackage: String? = null,
    val officialWebUrl: String,
    val installedVersion: String? = null
)

object MagcubicKnownBloatware {
    val defaultCatalog = listOf(
        BloatwarePackage(
            packageName = "com.htc.expandsdk",
            title = "Hotack ExpandSDK (Anúncios/Push)",
            description = "SDK secundário da fabricante Hotack que consome memória e rede em segundo plano.",
            impactBadge = "Alto Impacto"
        ),
        BloatwarePackage(
            packageName = "com.htc.storeos",
            title = "Loja Alternativa StoreOS",
            description = "Loja paralela da fabricante (você já tem a Google Play Store oficial com.android.vending).",
            impactBadge = "Economiza RAM"
        ),
        BloatwarePackage(
            packageName = "com.htc.hyk_test",
            title = "Hotack HYK Factory Test",
            description = "Aplicativo interno de teste de hardware deixado pela linha de montagem.",
            impactBadge = "Seguro Desativar"
        ),
        BloatwarePackage(
            packageName = "com.softwinner.dragonatt",
            title = "Allwinner DragonATT",
            description = "Módulo de automação de testes de fábrica do processador Allwinner.",
            impactBadge = "Seguro Desativar"
        ),
        BloatwarePackage(
            packageName = "com.softwinner.awlogsettings",
            title = "Allwinner Log Collector",
            description = "Serviço de gravação de logs internos da Allwinner.",
            impactBadge = "Leve"
        ),
        BloatwarePackage(
            packageName = "android.rockchip.update.service",
            title = "Rockchip Update Fantasma",
            description = "Serviço de atualização de outro fabricante (Rockchip) rodando sem utilidade.",
            impactBadge = "Seguro Desativar"
        ),
        BloatwarePackage(
            packageName = "com.htc.eventuploadservice",
            title = "OEM Event Upload Telemetry",
            description = "Serviço em segundo plano que envia relatórios constantes de telemetria.",
            impactBadge = "Alto Impacto"
        ),
        BloatwarePackage(
            packageName = "com.softwinner.dragonbox",
            title = "Allwinner DragonBox Test",
            description = "Suíte de diagnóstico de fábrica desnecessária no uso diário.",
            impactBadge = "Seguro Desativar"
        ),
        BloatwarePackage(
            packageName = "com.softwinner.agingdragonbox",
            title = "Allwinner Aging Mode",
            description = "Modo de teste colorido piscante de fábrica.",
            impactBadge = "Recomendado"
        ),
        BloatwarePackage(
            packageName = "com.mobisystems.office",
            title = "Office Suite Pré-instalado",
            description = "Pacote pesado de planilhas e textos raramente usado no projetor.",
            impactBadge = "Economiza RAM"
        )
    )
}
