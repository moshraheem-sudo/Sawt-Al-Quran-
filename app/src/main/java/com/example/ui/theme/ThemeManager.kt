package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean
) {
    EMERALD_DARK(
        id = "emerald_dark",
        titleAr = "الزمردي الذهبي (داكن ملكي)",
        subtitleAr = "الثيم الفخم باللون الأخضر الغامق والذهبي الملكي",
        primaryColor = Color(0xFFD4AF37),
        backgroundColor = Color(0xFF09120D),
        isDark = true
    ),
    ICE_BLUE_LIGHT(
        id = "ice_blue_light",
        titleAr = "السماوي الفضي (فاتح هادئ)",
        subtitleAr = "ثيم السحاب الهادئ باللون السماوي الناعم والأزرق الملكي",
        primaryColor = Color(0xFF2B5282),
        backgroundColor = Color(0xFFE8EFF8),
        isDark = false
    )
}

object ThemeManager {
    var currentThemeMode by mutableStateOf(AppThemeMode.EMERALD_DARK)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("quran_app_prefs", Context.MODE_PRIVATE)
        val savedId = prefs.getString("theme_mode", AppThemeMode.EMERALD_DARK.id)
        currentThemeMode = AppThemeMode.values().find { it.id == savedId } ?: AppThemeMode.EMERALD_DARK
    }

    fun setTheme(context: Context, mode: AppThemeMode) {
        currentThemeMode = mode
        val prefs = context.getSharedPreferences("quran_app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.id).apply()
    }
}
