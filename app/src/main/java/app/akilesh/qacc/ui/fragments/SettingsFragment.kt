package app.akilesh.qacc.ui.fragments

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import app.akilesh.qacc.Const.isOOS
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<ListPreference>("themePref")?.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            AppUtils.applyTheme(theme)
            true
        }

        if (SDK_INT < Q || isOOS)
            findPreference<SwitchPreferenceCompat>("separate_accent")?.isVisible = false

    }
}