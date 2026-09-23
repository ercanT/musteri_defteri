package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kullanıcının Açık/Karanlık Mod tercihini cihaz hafızasında (SharedPreferences) saklar.
 * Uygulama açılışında gecikmesiz, anında okunur ve kullanıcının seçimi kalıcı olarak korunur.
 */
class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_theme_preferences", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply()
        _isDarkMode.value = isDark
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    companion object {
        private const val KEY_IS_DARK_MODE = "user_selected_dark_mode"
    }
}
