package app.akilesh.qacc

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import app.akilesh.qacc.databinding.SettingsActivityBinding
import app.akilesh.qacc.utils.ThemeUtil

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsActivityBinding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsActivityBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(settingsActivityBinding.root)

        val decorView = window.decorView
        decorView.systemUiVisibility = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        when(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(settingsActivityBinding.settings.id, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val themePreference = findPreference<ListPreference>("themePref")
            if (themePreference != null) {
                themePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    val theme = newValue as String
                    ThemeUtil.applyTheme(theme)
                    true
                }
            }
        }
    }
}