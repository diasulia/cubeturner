package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity

object CubeHomeOverridePrefs {
    private const val PREF_FILE = "cube_home_override"
    private const val KEY_ENABLED = "override_enabled"

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun isAccessibilityServiceActive(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val myServiceId = "${context.packageName}/${CubeHomeAccessibilityService::class.java.name}"
        val shortServiceId = "${context.packageName}/.service.CubeHomeAccessibilityService"
        return enabledServices.contains(myServiceId, ignoreCase = true) ||
            enabledServices.contains(shortServiceId, ignoreCase = true)
    }
}

/**
 * Safely overrides Magcubic's hardcoded uid-0 stock launcher (`com.htc.htclauncherhighenglishd08`)
 * WITHOUT disabling the factory package, preventing firmware watchdog bootloops while making both
 * the physical Home button and power-on boot open CubeTuner's Apple TV stage seamlessly.
 */
class CubeHomeAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastRedirectElapsedMs = 0L

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.keyCode == KeyEvent.KEYCODE_HOME && CubeHomeOverridePrefs.isEnabled(this)) {
            if (event.action == KeyEvent.ACTION_UP) {
                launchCubeTunerHome()
            }
            return true
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        if (pkg == STOCK_MAGCUBIC_LAUNCHER && CubeHomeOverridePrefs.isEnabled(this)) {
            val uptime = SystemClock.elapsedRealtime()
            if (uptime < COLD_BOOT_GRACE_MS) {
                // Allow stock launcher to finish its initial Displayed callback so uid-0 watchdog is happy
                mainHandler.removeCallbacks(deferredLaunchRunnable)
                mainHandler.postDelayed(deferredLaunchRunnable, 1_100L)
            } else {
                launchCubeTunerHome()
            }
        }
    }

    override fun onInterrupt() {
        mainHandler.removeCallbacks(deferredLaunchRunnable)
    }

    private val deferredLaunchRunnable = Runnable {
        if (CubeHomeOverridePrefs.isEnabled(this)) {
            launchCubeTunerHome()
        }
    }

    private fun launchCubeTunerHome() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRedirectElapsedMs < 350L) return
        lastRedirectElapsedMs = now

        val homeIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        runCatching { startActivity(homeIntent) }
    }

    companion object {
        private const val STOCK_MAGCUBIC_LAUNCHER = "com.htc.htclauncherhighenglishd08"
        private const val COLD_BOOT_GRACE_MS = 35_000L
    }
}
