package com.example.pokegame.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.pokegame.R

object ThemeHelper {

    private const val PREF_THEME_KEY = "app_theme"

    fun getTheme(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isHighContrast = prefs.getBoolean("high_contrast", false)
        if (isHighContrast) {
            return R.style.Theme_Pokegame_HighContrast
        }

        val theme = prefs.getString(PREF_THEME_KEY, "default")
        return when (theme) {
            "red" -> R.style.Theme_Pokegame_Red
            "blue" -> R.style.Theme_Pokegame_Blue
            "system" -> R.style.Theme_Pokegame
            else -> R.style.Theme_Pokegame
        }
    }

    fun setTheme(context: Context, theme: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(PREF_THEME_KEY, theme).apply()
        applyNightMode(theme)
    }

    fun setHighContrast(context: Context, enabled: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean("high_contrast", enabled).apply()
        // High contrast overrides theme color, but we might want to ensure night mode is off or on
        // depending on design
        // For this implementation, we'll just let the theme style handle colors.
    }

    private fun applyNightMode(theme: String) {
        if (theme == "system") {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}
