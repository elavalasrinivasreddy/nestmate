package com.nestmate.app.core.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** App-wide theme preference. SYSTEM follows the OS setting. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Lightweight persisted app settings backed by SharedPreferences (no extra deps).
 * Exposes reactive state for Compose to observe.
 */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("nestmate_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    // ---- Chat read tracking (device-local; accurate unread badge without a
    // schema change or Firestore write. Background/killed push still needs FCM). ----

    private val _reads = MutableStateFlow(readAllReads())
    val reads: StateFlow<Map<String, Long>> = _reads.asStateFlow()

    fun markConversationRead(conversationId: String, at: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_READ_PREFIX + conversationId, at).apply()
        _reads.value = _reads.value + (conversationId to at)
    }

    fun lastReadAt(conversationId: String): Long =
        prefs.getLong(KEY_READ_PREFIX + conversationId, 0L)

    private fun readAllReads(): Map<String, Long> =
        prefs.all.entries
            .filter { it.key.startsWith(KEY_READ_PREFIX) && it.value is Long }
            .associate { it.key.removePrefix(KEY_READ_PREFIX) to (it.value as Long) }

    private fun readThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)

    /** Whether we've already shown the notification-permission ask once. */
    var notificationAsked: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ASKED, false)
        set(value) { prefs.edit().putBoolean(KEY_NOTIF_ASKED, value).apply() }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_READ_PREFIX = "read_"
        private const val KEY_NOTIF_ASKED = "notification_asked"
    }
}
