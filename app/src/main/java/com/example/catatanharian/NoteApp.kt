package com.example.catatanharian

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class NoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Terapkan mode gelap yang tersimpan sebelum Activity apa pun dibuat
        val mode = if (PrefsManager(this).isDarkMode())
            AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
