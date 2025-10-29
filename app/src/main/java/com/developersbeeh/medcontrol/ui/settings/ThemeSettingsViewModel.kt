package com.developersbeeh.medcontrol.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import com.developersbeeh.medcontrol.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    fun getCurrentTheme(): String {
        return userPreferences.getTheme()
    }

    fun setTheme(theme: String) {
        userPreferences.saveTheme(theme)
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}