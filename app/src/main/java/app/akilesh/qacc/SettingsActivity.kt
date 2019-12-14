package app.akilesh.qacc

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
                val colorWhite = ContextCompat.getColor(this, R.color.white)
                window.navigationBarColor = colorWhite
                window.statusBarColor = colorWhite
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.white))

            }
            Configuration.UI_MODE_NIGHT_YES -> {
                val colorDefaultDark = ContextCompat.getColor(this, R.color.colorDefaultDark)
                window.statusBarColor = colorDefaultDark
                window.navigationBarColor = colorDefaultDark
                supportActionBar?.setBackgroundDrawable(getDrawable(R.color.colorDefaultDark))
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(settingsActivityBinding.settings.id, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
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
                    ThemeUtil().applyTheme(theme)
                    true
                }
            }
        }
    }
}