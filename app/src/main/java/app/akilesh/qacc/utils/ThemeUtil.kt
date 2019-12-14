package app.akilesh.qacc.utils

import androidx.appcompat.app.AppCompatDelegate

class ThemeUtil {
    private val lightMode  = "light"
    private val darkMode = "dark"
    private val batterySaverMode = "battery"
    val default = "default"

    fun applyTheme(theme: String?) {
        when (theme) {
            lightMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            darkMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            batterySaverMode -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }

            default -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}
