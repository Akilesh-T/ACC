package app.akilesh.qacc.ui.fragments

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import app.akilesh.qacc.Const.isOOS
import app.akilesh.qacc.R
import app.akilesh.qacc.utils.AppUtils
import app.akilesh.qacc.utils.AppUtils.navAnim
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom.JSON

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

        findPreference<Preference>("prefUpdate")?.setOnPreferenceClickListener {
            AppUpdater(activity)
                .setUpdateFrom(JSON)
                .setUpdateJSON("https://raw.githubusercontent.com/Akilesh-T/ACC/master/app/update-changelog.json")
                .showAppUpdated(true)
                .setButtonDoNotShowAgain("")
                .setButtonDismiss("")
                .start()

            true
        }

        val backupPref = findPreference<Preference>("backups")!!
        backupPref.setOnPreferenceClickListener {
            findNavController().navigate(R.id.backup_fragment, null, navAnim)
            true
        }
    }
}

