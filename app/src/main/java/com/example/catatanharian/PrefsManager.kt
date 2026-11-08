package com.example.catatanharian

import android.content.Context
import android.content.SharedPreferences

/**
 * Membungkus SharedPreferences. Hanya menyimpan PENGATURAN kecil (key-value).
 * Isi catatan disimpan di database Room.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getFontSize(): String = prefs.getString(KEY_FONT_SIZE, FONT_MEDIUM) ?: FONT_MEDIUM

    fun setFontSize(size: String) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply()
    }

    // Folder yang sedang dipilih di halaman Folder (dibaca di onResume)
    fun getFolder(): String = prefs.getString(KEY_FOLDER, FOLDER_ALL) ?: FOLDER_ALL

    fun setFolder(folder: String) {
        prefs.edit().putString(KEY_FOLDER, folder).apply()
    }

    companion object {
        private const val PREFS_NAME = "catatan_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_FOLDER = "folder"

        const val FOLDER_ALL = "Semua"
        val FOLDERS = listOf("Pribadi", "Kuliah", "Pekerjaan")

        const val FONT_SMALL = "small"
        const val FONT_MEDIUM = "medium"
        const val FONT_LARGE = "large"
    }
}
